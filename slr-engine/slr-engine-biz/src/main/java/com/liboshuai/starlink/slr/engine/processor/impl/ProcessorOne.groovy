package com.liboshuai.starlink.slr.engine.processor.impl

import com.liboshuai.starlink.slr.engine.api.constants.GlobalConstants
import com.liboshuai.starlink.slr.engine.api.constants.RedisKeyConstants
import com.liboshuai.starlink.slr.engine.api.dto.EventKafkaDTO
import com.liboshuai.starlink.slr.engine.api.dto.ProcessorDTO
import com.liboshuai.starlink.slr.engine.api.dto.RuleConditionDTO
import com.liboshuai.starlink.slr.engine.api.dto.RuleInfoDTO
import com.liboshuai.starlink.slr.engine.api.enums.RuleConditionOperatorTypeEnum
import com.liboshuai.starlink.slr.engine.api.util.TemplatePlaceholderUtil
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
import org.apache.flink.api.java.tuple.Tuple2
import org.apache.flink.util.Collector
import org.apache.flink.util.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.LocalDateTime

/**
 * 运算机one
 */
class ProcessorOne implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ProcessorOne.class)

    /**
     * smallValue（窗口步长）: key为eventCode,value为eventValue和最新的EventKafkaDTO
     */
    private MapState<String, Tuple2<Long, EventKafkaDTO>> smallMapState

    /**
     * 记录对应eventCode是否已经初始化过（注意不要使用ListState，它查找指定元素的性能很差）
     */
    private MapState<String, Boolean> smallInitMapState

    /**
     * bigValue（窗口大小）: key为eventCode，小map的key为时间戳，小map的value为一个一个步长的eventValue累加值和最新的EventKafkaDTO
     */
    private MapState<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapState

    /**
     * 最近一次预警时间
     */
    private ValueState<Long> lastWarningTimeState

    @Override
    void init(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) {
        String ruleCode = ruleInfoDTO.getRuleCode()
        // 状态变量注册使用 ruleCode 作为后缀，以防止不同规则使用相同的模型导致状态变量数据冲突覆盖
        smallMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(
                        "smallMapState_${ruleCode}", Types.STRING,
                        Types.TUPLE(Types.LONG, Types.POJO(EventKafkaDTO.class))
                )
        )
        smallInitMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("smallInitMapState_${ruleCode}", Types.STRING, Types.BOOLEAN)
        )
        bigMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("bigMapState_${ruleCode}", Types.STRING,
                        Types.MAP(Types.LONG, Types.TUPLE(Types.LONG, Types.POJO(EventKafkaDTO.class))))
        )
        lastWarningTimeState = runtimeContext.getState(
                new ValueStateDescriptor<>("lastWarningTimeState_${ruleCode}", Types.LONG)
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
        if (ruleConditionList == null || ruleConditionList.isEmpty()) {
            throw new BusinessException("运算机 ruleConditionList 必须非空")
        }
        // 多个规则条件进行窗口值累加
        for (RuleConditionDTO ruleConditionDTO : ruleConditionList) {
            // 事件编号匹配不上，则直接跳过
            if (!Objects.equals(eventKafkaDTO.getEventCode(), ruleConditionDTO.getEventCode())) {
                continue
            }
            // 状态值防空
            if (smallMapState.get(eventKafkaDTO.getEventCode()) == null) {
                smallMapState.put(eventKafkaDTO.getEventCode(), Tuple2.of(0L, eventKafkaDTO))
            }
            if (ruleConditionDTO.getIsCrossHistory()) { //跨历史时间段
                LocalDateTime crossHistoryTimeline = ruleConditionDTO.getCrossHistoryTimeline()
                // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据
                if (eventKafkaDTO.getTimestamp() <= DateUtil.convertLocalDateTime2Timestamp(crossHistoryTimeline)) {
                    continue
                }
                // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，所以检查当前值是否已经通过redis初始化后，防止重复初始化
                if (!smallInitMapState.contains(eventKafkaDTO.getEventCode())) {
                    // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值（注意：Groovy字符串拼接的方式很麻烦，故使用StringBuilder）
                    String key = new StringBuilder().append(RedisKeyConstants.DORIS_HISTORY_VALUE)
                            .append(GlobalConstants.REDIS_KEY_SEPARATOR)
                            .append(ruleConditionDTO.getRuleCode())
                            .append(GlobalConstants.REDIS_KEY_SEPARATOR)
                            .append(ruleConditionDTO.getEventCode())
                    String keyCode = eventKafkaDTO.getKeyCode()
                    // 注意：因为上面获取历史缓存数据时，使用的是 <= 所以 redis 存储值时查询 doris 要包含历史截至时间点
                    String initValue = RedisUtil.hget(key, keyCode)
                    if (StringUtils.isNullOrWhitespaceOnly(initValue)) {
                        throw new BusinessException(StringUtil.format("从redis获取初始值必须非空, key:{}, hashKey: {}", key, keyCode))
                    }
                    smallMapState.put(eventKafkaDTO.getEventCode(), Tuple2.of(Long.parseLong(initValue), eventKafkaDTO))
                    smallInitMapState.put(eventKafkaDTO.getEventCode(), true)
                }
                // 从redis初始化值后，正常处理数据
                Tuple2<Long, EventKafkaDTO> currentTuple = smallMapState.get(eventKafkaDTO.getEventCode())
                Long newValue = currentTuple.f0 + Long.parseLong(eventKafkaDTO.getEventValue())
                smallMapState.put(eventKafkaDTO.getEventCode(), Tuple2.of(newValue, eventKafkaDTO))
            } else { // 非跨历史时间段
                // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据
                if (eventKafkaDTO.getTimestamp() != timestamp) {
                    continue
                }
                Tuple2<Long, EventKafkaDTO> currentTuple = smallMapState.get(eventKafkaDTO.getEventCode())
                Long newValue = currentTuple.f0 + Long.parseLong(eventKafkaDTO.getEventValue())
                smallMapState.put(eventKafkaDTO.getEventCode(), Tuple2.of(newValue, eventKafkaDTO))
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
        if (ruleConditionList == null || ruleConditionList.isEmpty()) {
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
        if (eventResult && (timestamp - lastWarningTimeState.value() >= ruleInfoDTO.getWarnInterval())) {
            lastWarningTimeState.update(timestamp)
            // 进行预警信息拼接组合
            String finalWarnMessage = TemplatePlaceholderUtil.replacePlaceholders(
                    ruleInfoDTO.getWarnMessage(),
                    ruleInfoDTO,
                    getLatestEventKafkaDto(),
                    getProcessorDto()
            )
            log.info("最终推送的预警信息内容：{}", finalWarnMessage)
            out.collect(finalWarnMessage)
        }
        // 调试使用，待删除
        logBigMapState(ruleInfoDTO.getRuleCode(), ruleConditionMapByEventCode.keySet(), null, bigMapState)
    }

    /**
     * 判断是否触发规则事件阈值。
     *
     * <p>此方法遍历 `bigMapState` 中的所有事件代码及其对应的时间戳和事件值累加，对每个事件代码
     * 的累加值与预定义的阈值进行比较。如果某个事件代码的累加值超过其阈值，则在结果映射中记录为 `true`。
     * 最后，根据 `ruleInfoDTO` 中指定的组合条件操作符（如 AND/OR）评估所有事件代码的结果，从而确定
     * 是否整体满足触发规则的条件。
     *
     * @param ruleConditionMapByEventCode 按事件代码分组的规则条件映射，每个事件代码对应一个 `RuleConditionDTO`
     * @param ruleInfoDTO 规则信息数据传输对象，包含组合条件操作符等规则配置
     * @return 如果根据组合条件操作符评估后满足规则阈值条件，返回 `true`；否则返回 `false`
     * @throws Exception 在评估过程中发生任何异常时抛出
     */
    private boolean evaluateEventThresholds(Map<String, RuleConditionDTO> ruleConditionMapByEventCode,
                                            RuleInfoDTO ruleInfoDTO) throws Exception {
        Map<String, Boolean> eventCodeAndWarnResult = new HashMap<>()
        for (Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapEntry : bigMapState.entries()) {
            String eventCode = bigMapEntry.getKey()
            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()
            long eventValueSum = timestampAndEventValueKafkaDtoMap.values().stream()
                    .map(o -> o.f0)
                    .mapToLong(Long::longValue)
                    .sum()
            RuleConditionDTO condition = ruleConditionMapByEventCode.get(eventCode)
            if (condition != null) {
                Long eventThreshold = condition.getEventThreshold()
                eventCodeAndWarnResult.put(eventCode, eventValueSum > eventThreshold)
            }
        }
        boolean eventResult = evaluateEventResults(eventCodeAndWarnResult, ruleInfoDTO.getCombinedConditionOperator())
        return eventResult
    }

    /**
     * 聚合 bigMapState 中的事件值并构建 ProcessorDTO 对象。
     *
     * <p>该方法遍历 bigMapState，其中每个键为 eventCode，值为一个包含时间戳和对应
     * Tuple2<Long, EventKafkaDTO> 的映射。对于每个 eventCode，方法会将所有时间戳
     * 下的 eventValue（Tuple2 中的第一个值）进行累加，生成一个 eventCode 与其
     * 累加值的映射。最后，基于这些聚合结果构建并返回一个包含 eventCodeAndValueSumMap 的 ProcessorDTO 对象。</p>
     *
     * @return 包含每个 eventCode 对应 eventValue 累加值的 ProcessorDTO 对象
     * @throws Exception 如果在处理过程中发生错误
     */
    private ProcessorDTO getProcessorDto() throws Exception {
        Map<String, Long> eventCodeAndValueSumMap = new HashMap<>()
        for (Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapEntry : bigMapState.entries()) {
            String eventCode = bigMapEntry.getKey()
            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()
            long eventValueSum = timestampAndEventValueKafkaDtoMap.values().stream()
                    .map(o -> o.f0)
                    .mapToLong(Long::longValue)
                    .sum()
            eventCodeAndValueSumMap.put(eventCode, eventValueSum)
        }
        ProcessorDTO processorDTO = ProcessorDTO.builder()
                .eventCodeAndValueSumMap(eventCodeAndValueSumMap)
                .build()
        return processorDTO
    }


    /**
     * 从所有事件条件累积的值流中检索最新的 Kafka 事件数据。
     *
     * <p>此方法遍历 `bigMapState` 中存储的所有事件数据，查找具有最大时间戳的 `EventKafkaDTO` 对象，
     * 并返回该最新的事件数据。
     *
     * @return 最新的 {@link EventKafkaDTO} 对象，如果没有事件数据则返回 {@code null}
     * @throws Exception 如果在遍历过程中发生异常
     */
    private EventKafkaDTO getLatestEventKafkaDto() throws Exception {
        // 初始化变量，用于存储最新的 EventKafkaDTO 和对应的最大时间戳
        EventKafkaDTO latestEventKafkaDTO = null;
        Long maxTimestamp = Long.MIN_VALUE;

        // 遍历 bigMapState 中的每一个大键（eventCode）及其对应的内部映射
        for (Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapEntry : bigMapState.entries()) {
            // 获取当前 eventCode 对应的时间戳与事件数据的映射
            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue();

            // 获取当前映射中的所有条目（时间戳与事件数据对）
            Set<Map.Entry<Long, Tuple2<Long, EventKafkaDTO>>> entrySet = timestampAndEventValueKafkaDtoMap.entrySet();

            // 遍历当前 eventCode 下的所有时间戳和事件数据对
            for (Map.Entry<Long, Tuple2<Long, EventKafkaDTO>> entry : entrySet) {
                Long currentTimestamp = entry.getKey(); // 当前条目的时间戳
                Tuple2<Long, EventKafkaDTO> value = entry.getValue(); // 包含累加值和事件数据的元组

                // 如果当前时间戳大于已记录的最大时间戳，则更新最大时间戳和最新的事件数据
                if (currentTimestamp > maxTimestamp) {
                    maxTimestamp = currentTimestamp;
                    latestEventKafkaDTO = value.f1; // 获取元组中的 EventKafkaDTO 对象
                }
            }
        }

        // 返回找到的最新的 EventKafkaDTO 对象
        return latestEventKafkaDTO;
    }


    /**
     * 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMapState中
     */
    private void updateBigMapWithSmallMap(long timestamp) throws Exception {
        for (Map.Entry<String, Tuple2<Long, EventKafkaDTO>> smallMapEntry : smallMapState.entries()) {
            String eventCode = smallMapEntry.getKey()
            Tuple2<Long, EventKafkaDTO> eventValueAndKafkaDtoTuple2 = smallMapEntry.getValue()
            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueMap = bigMapState.get(eventCode)
            if (timestampAndEventValueMap == null || timestampAndEventValueMap.isEmpty()) {
                timestampAndEventValueMap = new HashMap<>()
            }
            timestampAndEventValueMap.put(timestamp, eventValueAndKafkaDtoTuple2)
            bigMapState.put(eventCode, timestampAndEventValueMap)
        }
        // 当前窗口步长的数据已经添加到窗口中了，清空状态
        smallMapState.clear()
    }

    /**
     * 清理窗口大小之外的数据
     */
    private void cleanupWindowData(long timestamp, Map<String, RuleConditionDTO> ruleConditionMapByEventCode) throws Exception {
        for (Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapEntry : bigMapState.entries()) {
            String eventCode = bigMapEntry.getKey()
            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueMap = bigMapEntry.getValue()
            Long windowSize = ruleConditionMapByEventCode.get(eventCode).getWindowSize()
            long windowThresholdTime = timestamp - windowSize
            Iterator<Map.Entry<Long, Tuple2<Long, EventKafkaDTO>>> iterator = timestampAndEventValueMap.entrySet().iterator()
            while (iterator.hasNext()) {
                Map.Entry<Long, Tuple2<Long, EventKafkaDTO>> next = iterator.next()
                Long time = next.getKey()
                if (time <= windowThresholdTime) {
                    iterator.remove()
                }
            }
            bigMapState.put(eventCode, timestampAndEventValueMap)
        }
    }

    private void logBigMapState(String ruleCode, Set<String> eventCodeList, String keyCode, MapState<String,
            Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapState) throws Exception {
        Map<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMap = new HashMap<>()
        Iterator<Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>>> iterator = bigMapState.iterator()
        while (iterator.hasNext()) {
            Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> next = iterator.next()
            bigMap.put(next.getKey(), next.getValue())
        }
        log.warn("ProcessorOne对象onTimer方法结束 ruleCode={}, eventCodeList={}, keyCode={}, bigMapState={}",
                ruleCode, JsonUtil.toJsonString(eventCodeList), keyCode, JsonUtil.toJsonString(bigMap))
    }

    /**
     * 评估事件结果，根据给定的条件操作符返回最终结果。
     *
     * @param eventCodeAndWarnResult 包含事件代码及其对应的警告结果的映射
     * @param conditionOperator 条件操作符，支持 AND 和 OR
     * @return 根据条件操作符计算后的最终结果（true 或 false）
     */
    boolean evaluateEventResults(Map<String, Boolean> eventCodeAndWarnResult, Integer conditionOperator) {
        if (eventCodeAndWarnResult == null || eventCodeAndWarnResult.isEmpty()) {
            return false
        }
        if (eventCodeAndWarnResult.values() == null || eventCodeAndWarnResult.values().isEmpty()) {
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
