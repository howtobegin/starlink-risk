package com.liboshuai.starlink.slr.engine.processor.impl

import com.liboshuai.starlink.slr.engine.api.dto.EventKafkaDTO
import com.liboshuai.starlink.slr.engine.api.dto.RuleConditionDTO
import com.liboshuai.starlink.slr.engine.api.dto.RuleInfoDTO
import com.liboshuai.starlink.slr.engine.api.enums.RuleConditionOperatorTypeEnum
import com.liboshuai.starlink.slr.engine.api.util.TemplatePlaceholderUtil
import com.liboshuai.starlink.slr.engine.convert.EventKeyConvert
import com.liboshuai.starlink.slr.engine.dto.WarnInfoDTO
import com.liboshuai.starlink.slr.engine.exception.BusinessException
import com.liboshuai.starlink.slr.engine.processor.Processor
import com.liboshuai.starlink.slr.engine.utils.data.RedisUtil
import com.liboshuai.starlink.slr.engine.utils.date.DateUtil
import com.liboshuai.starlink.slr.engine.utils.string.JsonUtil
import com.liboshuai.starlink.slr.engine.utils.string.StringUtil
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.state.MapState
import org.apache.flink.api.common.state.MapStateDescriptor
import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.common.typeinfo.Types
import org.apache.flink.util.CollectionUtil
import org.apache.flink.util.Collector
import org.apache.flink.util.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.LocalDateTime

import com.liboshuai.starlink.slr.engine.api.constants.*

/**
 * 运算机one
 */
class ProcessorOne implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ProcessorOne.class)

    /**
     * smallValue（窗口步长）: key为eventCode,value为eventValue
     */
    private MapState<String, Long> smallMapState

    /**
     * 记录对应eventCode是否已经初始化过
     */
    private MapState<String, Object> smallInitMapState

    /**
     * bigValue（窗口大小）: key为eventCode，小map的key为时间戳，小map的value为一个一个步长的eventValue累加值
     */
    private MapState<String, Map<Long, Long>> bigMapState

    /**
     * 最近一次预警时间
     */
    private ValueState<Long> lastWarningTimeState

    /**
     * key详情信息（主要用于预警信息）
     */
    private ValueState<WarnInfoDTO> warnInfoState

    @Override
    void init(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) {
        String ruleCode = ruleInfoDTO.getRuleCode()
        smallMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("smallMapState_${ruleCode}", String.class, Long.class)
        )
        smallInitMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("smallInitMapState_${ruleCode}", String.class, Object.class)
        )
        bigMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("bigMapState_${ruleCode}", Types.STRING,
                        Types.MAP(Types.LONG, Types.LONG))
        )
        lastWarningTimeState = runtimeContext.getState(
                new ValueStateDescriptor<>("lastWarningTimeState_${ruleCode}", Long.class)
        )
        warnInfoState = runtimeContext.getState(
                new ValueStateDescriptor<>("warnInfoState_${ruleCode}", WarnInfoDTO.class)
        )
    }

    @Override
    void processElement(long timestamp, EventKafkaDTO eventKafkaDTO, RuleInfoDTO ruleInfoDTO, Collector<String> out)
            throws Exception {
        if (Objects.isNull(ruleInfoDTO)) {
            throw new BusinessException("运算机 ruleInfoDTO 必须非空")
        }
        String eventKafkaDTOChannel = eventKafkaDTO.getChannel()
        String ruleInfoChannel = ruleInfoDTO.getChannel()
        if (!Objects.equals(eventKafkaDTOChannel, ruleInfoChannel)) {
            return
        }
        // 获取规则条件
        List<RuleConditionDTO> ruleConditionList = ruleInfoDTO.getRuleConditionGroup()
        if (CollectionUtil.isNullOrEmpty(ruleConditionList as Map)) {
            throw new BusinessException("运算机 ruleConditionList 必须非空")
        }
        // 多个规则条件进行窗口值累加
        for (RuleConditionDTO ruleConditionDTO : ruleConditionList) {
            if (Objects.equals(eventKafkaDTO.getEventCode(), ruleConditionDTO.getEventCode())) { // 事件编号匹配上
                // 更新预警所需信息
                WarnInfoDTO warnInfoDTO = EventKeyConvert.INSTANCE.eventKafkaDTO2WarnInfoDTO(eventKafkaDTO)
                warnInfoState.update(warnInfoDTO)
                // 状态值防空
                if (smallMapState.get(eventKafkaDTO.getEventCode()) == null) {
                    smallMapState.put(eventKafkaDTO.getEventCode(), 0L)
                }
                if (ruleConditionDTO.getIsCrossHistory()) { //跨历史时间段
                    LocalDateTime crossHistoryTimeline = ruleConditionDTO.getCrossHistoryTimeline()
                    // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据
                    if (eventKafkaDTO.getTimestamp() > DateUtil.convertLocalDateTime2Timestamp(crossHistoryTimeline)) {
                        // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，所以检查当前值是否已经通过redis初始化后，防止重复初始化
                        if (!smallInitMapState.contains(eventKafkaDTO.getEventCode())) {
                            // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值（注意：Groovy字符串拼接的方式不一样）
                            String key = "${RedisKeyConstants.DORIS_HISTORY_VALUE}${GlobalConstants.REDIS_KEY_SEPARATOR}${ruleConditionDTO.getRuleCode()}${GlobalConstants.REDIS_KEY_SEPARATOR}${ruleConditionDTO.getEventCode()}"
                            String keyCode = eventKafkaDTO.getKeyCode()
                            String initValue = RedisUtil.hget(key, keyCode)
                            if (StringUtils.isNullOrWhitespaceOnly(initValue)) {
                                throw new BusinessException(StringUtil.format("从redis获取初始值必须非空, key:{}, hashKey: {}", key, keyCode))
                            }
                            smallMapState.put(eventKafkaDTO.getEventCode(), Long.parseLong(initValue))
                            smallInitMapState.put(eventKafkaDTO.getEventCode(), null)
                        }
                        // 从redis初始化值后，正常处理数据
                        smallMapState.put(eventKafkaDTO.getEventCode(),
                                smallMapState.get(eventKafkaDTO.getEventCode()) + Long.parseLong(eventKafkaDTO.getEventValue()))
                    }
                } else { // 非跨历史时间段
                    // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据
                    if (eventKafkaDTO.getTimestamp() == timestamp) {
                        smallMapState.put(eventKafkaDTO.getEventCode(),
                                smallMapState.get(eventKafkaDTO.getEventCode()) + Long.parseLong(eventKafkaDTO.getEventValue()))
                    }
                }
            }
        }
    }

    @Override
    void onTimer(long timestamp, RuleInfoDTO ruleInfoDTO, Collector<String> out) throws Exception {
        if (Objects.isNull(ruleInfoDTO)) {
            throw new BusinessException("运算机 ruleInfoDTO 必须非空")
        }
        // 获取规则条件
        List<RuleConditionDTO> ruleConditionList = ruleInfoDTO.getRuleConditionGroup()
        if (CollectionUtil.isNullOrEmpty(ruleConditionList as Map)) {
            throw new BusinessException("运算机 ruleConditionList 必须非空")
        }
        // 将规则条件根据事件编号存储到map中，方便后续操作
        Map<String, RuleConditionDTO> ruleConditionMapByEventCode = new HashMap<>()
        for (RuleConditionDTO ruleConditionDTO : ruleConditionList) {
            ruleConditionMapByEventCode.put(ruleConditionDTO.getEventCode(), ruleConditionDTO)
        }
        // 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMapState中
        updateBigMapWithSmallMap(timestamp)
        // 清理窗口大小之外的数据
        cleanupWindowData(timestamp, ruleConditionMapByEventCode)
        // 判断是否触发规则事件阈值
        boolean eventResult = evaluateEventThresholds(ruleConditionMapByEventCode, ruleInfoDTO)
        // 根据规则中事件条件表达式组合判断事件结果 与预警频率 判断否是触发预警
        if (lastWarningTimeState.value() == null) {
            lastWarningTimeState.update(0L)
        }
        WarnInfoDTO warnInfoDTO = warnInfoState.value()
        if (eventResult && (timestamp - lastWarningTimeState.value() >= ruleInfoDTO.getWarnInterval())) {
            lastWarningTimeState.update(timestamp)
            // TODO: 进行预警信息拼接组合
            String finalWarnMessage = TemplatePlaceholderUtil.replacePlaceholders(ruleInfoDTO.getWarnMessage(),
                    RuleInfoDTO)
            log.info("最终推送的预警信息内容：{}", finalWarnMessage)
            out.collect(finalWarnMessage)
        }
        // 调试使用，待删除
        logBigMapState(ruleInfoDTO.getRuleCode(), ruleConditionMapByEventCode.keySet(),
                warnInfoDTO == null ? null : warnInfoDTO.getKeyCode(), bigMapState)
    }

    /**
     * 判断是否触发规则事件阈值
     */
    private boolean evaluateEventThresholds(Map<String, RuleConditionDTO> ruleConditionMapByEventCode, RuleInfoDTO ruleInfoDTO) throws Exception {
        Map<String, Boolean> eventCodeAndWarnResult = new HashMap<>()
        for (Map.Entry<String, Map<Long, Long>> bigMapEntry : bigMapState.entries()) {
            String eventCode = bigMapEntry.getKey()
            Map<Long, Long> timestampAndEventValueMap = bigMapEntry.getValue()
            long eventValueSum = timestampAndEventValueMap.values().stream().mapToLong(Long::longValue).sum()
            Long eventThreshold = ruleConditionMapByEventCode.get(eventCode).getEventThreshold()
            eventCodeAndWarnResult.put(eventCode, eventValueSum > eventThreshold)
        }
        boolean eventResult = evaluateEventResults(eventCodeAndWarnResult, ruleInfoDTO.getCombinedConditionOperator())
        return eventResult
    }

    /**
     * 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMapState中
     */
    private void updateBigMapWithSmallMap(long timestamp) throws Exception {
        for (Map.Entry<String, Long> smallMapEntry : smallMapState.entries()) {
            String eventCode = smallMapEntry.getKey()
            Long eventValue = smallMapEntry.getValue()
            Map<Long, Long> timestampAndEventValueMap = bigMapState.get(eventCode)
            if (CollectionUtil.isNullOrEmpty(timestampAndEventValueMap as Map)) {
                timestampAndEventValueMap = new HashMap<>()
            }
            timestampAndEventValueMap.put(timestamp, eventValue)
            bigMapState.put(eventCode, timestampAndEventValueMap)
        }
        // 当前窗口步长的数据已经添加到窗口中了，清空状态
        smallMapState.clear()
    }

    /**
     * 清理窗口大小之外的数据
     */
    private void cleanupWindowData(long timestamp, Map<String, RuleConditionDTO> ruleConditionMapByEventCode) throws Exception {
        for (Map.Entry<String, Map<Long, Long>> bigMapEntry : bigMapState.entries()) {
            String eventCode = bigMapEntry.getKey()
            Map<Long, Long> timestampAndEventValueMap = bigMapEntry.getValue()
            Long windowSize = ruleConditionMapByEventCode.get(eventCode).getWindowSize()
            long twentyMinutesAgo = timestamp - windowSize
            Iterator<Map.Entry<Long, Long>> iterator = timestampAndEventValueMap.entrySet().iterator()
            while (iterator.hasNext()) {
                Map.Entry<Long, Long> next = iterator.next()
                Long time = next.getKey()
                if (time <= twentyMinutesAgo) {
                    iterator.remove()
                }
            }
            bigMapState.put(eventCode, timestampAndEventValueMap)
        }
    }

    private void logSmallMapState(String ruleCode, List<String> eventCodeList, String keyCode,
                                  MapState<String, Long> smallMapState) throws Exception {
        Map<String, Long> smallMap = new HashMap<>()
        Iterator<Map.Entry<String, Long>> iterator = smallMapState.iterator()
        while (iterator.hasNext()) {
            Map.Entry<String, Long> next = iterator.next()
            smallMap.put(next.getKey(), next.getValue())
        }
        log.warn("ProcessorOne对象processElement方法结束 ruleCode={}, eventCodeList={}, keyCode={}, smallMapState={}",
                ruleCode, JsonUtil.toJsonString(eventCodeList), keyCode, JsonUtil.toJsonString(smallMap))
    }

    private void logBigMapState(String ruleCode, Set<String> eventCodeList, String keyCode, MapState<String,
            Map<Long, Long>> bigMapState) throws Exception {
        Map<String, Map<Long, Long>> bigMap = new HashMap<>()
        Iterator<Map.Entry<String, Map<Long, Long>>> iterator = bigMapState.iterator()
        while (iterator.hasNext()) {
            Map.Entry<String, Map<Long, Long>> next = iterator.next()
            bigMap.put(next.getKey(), next.getValue())
        }
        log.warn("ProcessorOne对象onTimer方法结束 ruleCode={}, eventCodeList={}, keyCode={}, bigMapState={}",
                ruleCode, JsonUtil.toJsonString(eventCodeList), keyCode, JsonUtil.toJsonString(bigMap))
    }

    /**
     * 评估事件结果，根据给定的条件操作符返回最终结果。
     *
     * @param eventCodeAndWarnResult 包含事件代码及其对应的警告结果的映射
     * @param conditionOperator      条件操作符，支持 AND 和 OR
     * @return 根据条件操作符计算后的最终结果（true 或 false）
     */
    static boolean evaluateEventResults(Map<String, Boolean> eventCodeAndWarnResult, Integer conditionOperator) {
        if (CollectionUtil.isNullOrEmpty(eventCodeAndWarnResult as Map)) {
            return false
        }
        if (CollectionUtil.isNullOrEmpty(eventCodeAndWarnResult.values() as Map)) {
            return false
        }
        // 初始化结果变量，根据条件操作符判断初始值
        boolean result = conditionOperator == RuleConditionOperatorTypeEnum.AND.getCode()

        // 遍历事件结果的 Map
        for (Boolean eventResult : eventCodeAndWarnResult.values()) {
            if (conditionOperator == RuleConditionOperatorTypeEnum.AND.getCode()) {
                // 对于 AND，只有当所有结果都为 true 时，结果才为 true
                result = eventResult
                // 提前结束循环，如果结果已经为 false
                if (!result) {
                    break
                }
            } else if (conditionOperator == RuleConditionOperatorTypeEnum.OR.getCode()) {
                // 对于 OR，只要有一个结果为 true，结果就为 true
                result = eventResult
                // 提前结束循环，如果结果已经为 true
                if (result) {
                    break
                }
            }
        }
        // 返回最终的评估结果
        return result
    }

}
