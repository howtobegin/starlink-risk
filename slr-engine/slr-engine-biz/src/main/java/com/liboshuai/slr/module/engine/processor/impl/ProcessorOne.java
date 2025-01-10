package com.liboshuai.slr.module.engine.processor.impl;

import com.liboshuai.slr.framework.common.constants.RedisKeyConstants;
import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.module.engine.dto.*;
import com.liboshuai.slr.module.engine.enums.RuleCondCombOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleCondTypeEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import com.liboshuai.slr.module.engine.framework.exception.BusinessException;
import com.liboshuai.slr.module.engine.processor.Processor;
import com.liboshuai.slr.module.engine.utils.*;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.Collector;
import org.apache.flink.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 运算机one
 */
public class ProcessorOne implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ProcessorOne.class);

    /**
     * smallValue（窗口步长）: key为eventField,value为eventValue和最新的EventKafkaDTO
     */
    private Map<String, Map<String, Tuple2<Long, KafkaEventDTO>>> smallMap;
    /**
     * 记录对应eventField是否已经初始化过（注意不要使用ListState，它查找指定元素的性能很差）
     */
    private MapState<String, Boolean> smallInitMapState;
    /**
     * 对应 keyCode + keyValue 最近一次预警时间
     */
    private ValueState<Long> lastWarningTimeState;
    /**
     * bigValue（窗口大小）: key为eventField，小map的key为时间戳，小map的value为一个一个步长的eventValue累加值和最新的EventKafkaDTO
     */
    private MapState<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>> bigMapState;

    private void logState(Long ruleCode, String currentKey) throws Exception {
//        // smallInitMapState
//        Map<String, Boolean> smallInitMap = new HashMap<>();
//        Iterator<Map.Entry<String, Boolean>> oldSmallInitMapIterator = smallInitMapState.iterator();
//        while (oldSmallInitMapIterator.hasNext()) {
//            Map.Entry<String, Boolean> next = oldSmallInitMapIterator.next();
//            smallInitMap.put(next.getKey(), next.getValue());
//        }
//        // lastWarningTimeState
//        Long lastWarningTime = lastWarningTimeState.value();
        // bigMapState
        Map<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>> bigMap = new HashMap<>();
        for (Map.Entry<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>> entry : bigMapState.entries()) {
            bigMap.put(entry.getKey(), entry.getValue());
        }
        log.warn("onTime计算触发，ruleCode:{}, currentKey：{}, bigMap：{}", ruleCode, currentKey, bigMap);
    }

    /**
     * 初始化方法，用于在运行时上下文中注册各种状态
     *
     * @param runtimeContext 运行时上下文，用于访问状态和其它运行时设施
     * @param ruleInfoDTO    规则信息数据传输对象，包含规则特定的元数据
     * @throws Exception 如果初始化过程中发生错误则抛出异常
     */
    @Override
    public void init(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) throws Exception {
        Long ruleCode = ruleInfoDTO.getRuleCode();
        Long ruleVersion = ruleInfoDTO.getRuleVersion();
        // 状态变量注册使用 ruleCode + ruleVersion 作为后缀，以防止不同规则使用相同的模型导致状态变量数据冲突覆盖
        smallMap = new HashMap<>();
        String smallInitMapStateName = "smallInitMapState_" + ruleCode + "_" + ruleVersion;
        smallInitMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(smallInitMapStateName, Types.STRING, Types.BOOLEAN)
        );
        String bigMapStateName = "bigMapState_" + ruleCode + "_" + ruleVersion;
        bigMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(bigMapStateName, Types.TUPLE(Types.STRING, Types.LONG), Types.TUPLE(Types.LONG, Types.POJO(KafkaEventDTO.class)))
        );
        String lastWarningTimeStateName = "lastWarningTimeState_" + ruleCode + "_" + ruleVersion;
        lastWarningTimeState = runtimeContext.getState(
                new ValueStateDescriptor<>(lastWarningTimeStateName, Types.LONG)
        );
    }

    /**
     * 处理元素事件，根据给定的规则信息和Kafka事件进行处理
     *
     * @param currentEventTimestamp 时间戳，用于处理的时间参考
     * @param ruleInfoDTO           规则信息数据传输对象，包含规则的详细信息
     * @param kafkaEventDTO         Kafka事件数据传输对象，包含事件的详细信息
     * @param out                   用于输出处理结果的收集器
     * @throws Exception 如果处理过程中遇到任何错误，则抛出异常
     */
    @Override
    public void processElement(String currentKey, long currentEventTimestamp, RuleInfoDTO ruleInfoDTO, KafkaEventDTO kafkaEventDTO,
                               Collector<ResultDTO> out) throws Exception {
        if (Objects.isNull(ruleInfoDTO)) {
            throw new BusinessException("运算机 ruleInfoDTO 必须非空");
        }
        if (!Objects.equals(ruleInfoDTO.getRuleStatus(), CommonStatusEnum.ONLINE.getCode())
                && !Objects.equals(ruleInfoDTO.getRuleStatus(), CommonStatusEnum.OFFLINE_PENDING.getCode())) {
            log.warn("加载到运算机池中的规则状态必须为'已上线'或'下线待审核'！规则编号：{}", ruleInfoDTO.getRuleCode());
            return;
        }
        // 事件与规则渠道匹配不上，则直接跳过
        if (!Objects.equals(kafkaEventDTO.getChannel(), ruleInfoDTO.getChannel())) {
            return;
        }
        // 事件与规则目标匹配不上，则直接跳过
        if (!Objects.equals(kafkaEventDTO.getTargetField(), ruleInfoDTO.getTargetField())) {
            return;
        }
        // 获取规则条件
        List<RuleCondDTO> condGroupList = ruleInfoDTO.getRuleCondGroup();
        if (condGroupList == null || condGroupList.isEmpty()) {
            throw new BusinessException("运算机 condGroupList 必须非空");
        }
        // 此模型仅支持条件为周期类型的规则
        for (RuleCondDTO condGroupDTO : condGroupList) {
            String type = condGroupDTO.getCondType();
            if (!Objects.equals(type, RuleCondTypeEnum.PERIODIC.getCode())) {
                log.warn("ProcessorOne 模型仅支持条件为周期类型的规则！规则编号：{}", ruleInfoDTO.getRuleCode());
                return;
            }
        }
        // 计算规则条件值
        processRuleCondValue(currentKey, currentEventTimestamp, ruleInfoDTO, kafkaEventDTO, out);
    }

    /**
     * 处理规则条件值
     * 该方法主要用于处理一组规则条件DTO，通过与Kafka事件DTO进行匹配来更新状态值
     * 如果规则条件跨越历史时间段，则需要从Redis中获取历史事件值，并进行初始化
     *
     * @param currentEventTimestamp 时间戳，用于非跨历史时间段的事件匹配
     * @param kafkaEventDTO         Kafka事件DTO
     */
    private void processRuleCondValue(String currentKey, long currentEventTimestamp, RuleInfoDTO ruleInfoDTO,
                                      KafkaEventDTO kafkaEventDTO, Collector<ResultDTO> out) throws Exception {
        List<RuleCondDTO> ruleCondGroup = ruleInfoDTO.getRuleCondGroup();
        for (RuleCondDTO ruleCondDTO : ruleCondGroup) {
            // 事件与规则中的事件编号匹配不上，则直接跳过
            if (!Objects.equals(kafkaEventDTO.getEventField(), ruleCondDTO.getEventField())) {
                // 事件编号匹配不上，则直接跳过
                continue;
            }
            // 进行事件属性匹配
            List<RuleEventAttrValueDTO> ruleEventAttrValueGroup = ruleCondDTO.getRuleEventAttrValueGroup();
            boolean eventAttributeMatchResult = matchEventAttribute(ruleEventAttrValueGroup, kafkaEventDTO);
            if (!eventAttributeMatchResult) {
                // 事件属性匹配不上，则直接跳过
                continue;
            }
            // 规则状态的key历史记录
            RuleKeyHistoryDTO keyDTO = RuleKeyHistoryDTO.builder()
                    .ruleCode(ruleInfoDTO.getRuleCode())
                    .ruleVersion(ruleInfoDTO.getRuleVersion())
                    .channel(ruleInfoDTO.getChannel())
                    .targetField(kafkaEventDTO.getTargetField())
                    .targetValue(kafkaEventDTO.getTargetValue())
                    .build();
            out.collect(ResultDTO.builder().ruleKeyHistoryDTO(keyDTO).build());
            // 状态值防空
            smallMap.computeIfAbsent(currentKey, key -> {
                Map<String, Tuple2<Long, KafkaEventDTO>> newMap = new HashMap<>();
                newMap.put(kafkaEventDTO.getEventField(), Tuple2.of(0L, kafkaEventDTO));
                return newMap;
            });
            if (ruleCondDTO.getCrossHistory()) { //跨历史时间段
                String crossHistoryTimeline = ruleCondDTO.getCrossHistoryTimeline();
                // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，
                // 所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据
                if (kafkaEventDTO.getEventTime()
                        <= DateUtil.convertString2Timestamp(crossHistoryTimeline)) {
                    continue;
                }
                // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，
                // 所以检查当前值是否已经通过redis初始化后，防止重复初始化
                if (!smallInitMapState.contains(kafkaEventDTO.getEventField())) {
                    // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值
                    // （注意：Groovy字符串拼接的方式很麻烦，故使用StringBuilder）
                    String redisKey = buildRedisKey(ruleCondDTO);
                    String redisHashKey = buildRedisHashKey(kafkaEventDTO);
                    // 注意：因为上面获取历史缓存数据时，使用的是 <= 所以 redis 存储值时查询 doris 要包含历史截至时间点
                    String initValue = RedisUtil.hget(redisKey, redisHashKey);
                    RedisUtil.hdel(redisKey, redisHashKey);
                    if (StringUtils.isNullOrWhitespaceOnly(initValue)) {
                        throw new BusinessException(
                                StringUtil.format("从redis获取初始值必须非空, redisKey:{}, hashKey: {}", redisKey, redisHashKey)
                        );
                    }
                    Map<String, Tuple2<Long, KafkaEventDTO>> stringTuple2Map = smallMap.get(currentKey);
                    stringTuple2Map.put(kafkaEventDTO.getEventField(), Tuple2.of(Long.parseLong(initValue), kafkaEventDTO));
                    smallMap.put(currentKey, stringTuple2Map);
                    smallInitMapState.put(kafkaEventDTO.getEventField(), true);
                }
                // 从redis初始化值后，正常处理数据
                addEventValue(currentKey, kafkaEventDTO);
            } else { // 非跨历史时间段
                // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据
                if (kafkaEventDTO.getEventTime() != currentEventTimestamp) {
                    continue;
                }
                addEventValue(currentKey, kafkaEventDTO);
            }
        }
    }

    private void addEventValue(String currentKey, KafkaEventDTO kafkaEventDTO) {
        Map<String, Tuple2<Long, KafkaEventDTO>> stringTuple2Map = smallMap.get(currentKey);
        Long currentValue = stringTuple2Map.get(kafkaEventDTO.getEventField()).f0;
        Long newValue = currentValue + Long.parseLong(kafkaEventDTO.getEventValue());
        stringTuple2Map.put(kafkaEventDTO.getEventField(), Tuple2.of(newValue, kafkaEventDTO));
        smallMap.put(currentKey, stringTuple2Map);
    }


    /**
     * 构建Redis的哈希键
     */
    private String buildRedisHashKey(KafkaEventDTO kafkaEventDTO) {
        String targetField = kafkaEventDTO.getTargetField();
        String targetValue = kafkaEventDTO.getTargetValue();
        return targetField +
                RedisKeyConstants.REDIS_KEY_SPLIT +
                targetValue;
    }

    /**
     * 构建Redis的key
     */
    private String buildRedisKey(RuleCondDTO ruleCondDTO) {
        return RedisKeyConstants.REDIS_KEY_PREFIX +
                RedisKeyConstants.REDIS_KEY_SPLIT +
                RedisKeyConstants.DORIS_EVENT_HISTORY_VALUE +
                RedisKeyConstants.REDIS_KEY_SPLIT +
                ruleCondDTO.getRuleCode() +
                RedisKeyConstants.REDIS_KEY_SPLIT +
                ruleCondDTO.getEventField();
    }

    /**
     * 匹配规则事件属性与Kafka事件属性是否符合
     * 此方法的目的是为了验证给定的Kafka事件是否满足规则事件中定义的所有属性条件
     * 它通过比较规则事件属性和Kafka事件属性来确定两者是否匹配
     *
     * @param kafkaEventDTO Kafka事件DTO，包含Kafka事件的详细信息，包括事件属性
     * @return boolean 如果Kafka事件属性与规则事件属性完全匹配，则返回true；否则返回false
     */
    private boolean matchEventAttribute(List<RuleEventAttrValueDTO> ruleEventAttrValueGroup, KafkaEventDTO kafkaEventDTO) {
        if (CollectionUtil.isEmptyOrContainsNulls(ruleEventAttrValueGroup)) {
            // 规则中不包含事件属性相关的配置，则表明不需要进行事件属性匹配，直接跳过即可
            return true;
        }
        // 逐一便利验证事件属性
        for (RuleEventAttrValueDTO ruleEventAttrValueDTO : ruleEventAttrValueGroup) {
            String attrValue = ruleEventAttrValueDTO.getAttrValue();
            if (StringUtils.isNullOrWhitespaceOnly(attrValue)) {
                // 规则中不包含事件属性值相关的配置，则表明不需要进行事件属性值匹配，直接跳过即可
                continue;
            }
            String attrField = ruleEventAttrValueDTO.getAttrField();
            Map<String, String> kafkaEventAttrMap = kafkaEventDTO.getEventAttrMap();
            if (CollectionUtil.isEmpty(kafkaEventAttrMap)) {
                // 规则包含事件属性配置，但是kafka数据事件属性map为空，故直接判定为不符合规则要求
                log.warn("规则包含事件属性配置，但是kafka数据事件属性map为空，故直接判定为不符合规则要求！" +
                        "规则事件属性信息:{}, kafka事件信息:{}", ruleEventAttrValueDTO, kafkaEventDTO);
                return false;
            }
            if (!kafkaEventAttrMap.containsKey(attrField)) {
                // kafka事件属性不包含规则中事件属性，则表明不符合匹配
                log.warn("kafka数据事件属性map并不包含规则配置的事件属性Field，故直接判定为不符合规则要求！" +
                        "规则事件属性信息:{}, kafka事件信息:{}", ruleEventAttrValueDTO, kafkaEventDTO);
                return false;
            }
            String kafkaEventAttributeValue = kafkaEventAttrMap.get(attrField);
            if (Objects.isNull(kafkaEventAttributeValue)) {
                // kafka事件中对于规则中事件属性值为空，则表明不符合匹配
                return false;
            }
            // 比较kafka中属性值与规则中属性值
            boolean isMatch = RuleEventAttrCompUtil.compareValues(ruleEventAttrValueDTO, kafkaEventDTO);
            if (!isMatch) {
                // kafka事件属性值与规则事件属性值不相等，则表明不符合匹配
                return false;
            }
        }
        // 所有事件属性都匹配，则表明符合匹配
        return true;
    }

    /**
     * 定时器触发时执行的方法
     *
     * @param timestamp   时间戳，表示当前时间
     * @param ruleInfoDTO 规则信息DTO，包含规则相关数据
     * @param out         输出收集器，用于收集和输出预警信息
     * @throws Exception 可能抛出的异常
     */
    @Override
    public boolean onTimer(String currentKey, long timestamp, RuleInfoDTO ruleInfoDTO, Collector<ResultDTO> out) throws Exception {
        if (Objects.isNull(ruleInfoDTO)) {
            throw new BusinessException("运算机 ruleInfoDTO 必须非空");
        }
        // 获取规则条件
        List<RuleCondDTO> groupGroup = ruleInfoDTO.getRuleCondGroup();
        if (groupGroup == null || groupGroup.isEmpty()) {
            throw new BusinessException("运算机 groupGroup 必须非空");
        }
        // 将规则条件根据事件编号存储到map中，方便后续操作
        Map<String, RuleCondDTO> ruleConditionMapByEventField = new HashMap<>();
        for (RuleCondDTO ruleCondDTO : groupGroup) {
            ruleConditionMapByEventField.put(ruleCondDTO.getEventField(), ruleCondDTO);
        }
        // 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMapState中
        updateBigMapWithSmallMap(currentKey, timestamp);
        // 清理窗口大小之外的数据
        cleanupWindowData(timestamp, ruleConditionMapByEventField);
        // 处理bigMapState
        Tuple3<Boolean, KafkaEventDTO, ProcessorDTO> processBigMapResult = processBigMap(ruleConditionMapByEventField, ruleInfoDTO.getRuleCondCombOp());
        // 根据规则中事件条件表达式组合判断事件结果 与预警频率 判断否是触发预警
        if (lastWarningTimeState.value() == null) {
            lastWarningTimeState.update(0L);
        }
        // 获取预警间隔时间，单位为毫秒
        long alertInterval = TimeUtil.toMillis(
                ruleInfoDTO.getAlertIntervalValue(), TimeUnitEnum.fromEnUnit(ruleInfoDTO.getAlertIntervalUnit())
        );
        // 触发结果为true，且当前时间减去上次预警时间大于预警间隔时间，则进行预警
        if (processBigMapResult.f0 && (timestamp - lastWarningTimeState.value() >= alertInterval)) {
            lastWarningTimeState.update(timestamp);
            // 进行预警信息拼接组合
            String finalWarnMessage = TemplateUtil.replacePlaceholders(
                    ruleInfoDTO.getAlertMessage(),
                    ruleInfoDTO,
                    processBigMapResult.f1,
                    processBigMapResult.f2
            );
            AlertMessageDTO alertMessageDTO = AlertMessageDTO.builder()
                    .channel(ruleInfoDTO.getChannel())
                    .ruleCode(ruleInfoDTO.getRuleCode())
                    .alertMessage(finalWarnMessage)
                    .alertTime(DateUtil.convertTimestamp2LocalDateTime(System.currentTimeMillis()))
                    .build();
            log.warn("当前Key: {}, 最终推送的预警信息内容：{}", currentKey, alertMessageDTO);
            ResultDTO resultDTO = ResultDTO.builder().alertMessageDTO(alertMessageDTO).build();
            out.collect(resultDTO);
        }
        logState(ruleInfoDTO.getRuleCode(), currentKey);
        return hasActiveEvents();
    }

    /**
     * 检查是否存在活跃的事件
     * 该方法用于遍历一个大的状态映射，以确定其中是否包含活跃的Kafka事件
     *
     * @return boolean - 如果存在活跃的事件，则返回true；否则返回false
     */
    private boolean hasActiveEvents() throws Exception {
        return bigMapState.entries().iterator().hasNext();
    }

    /**
     * 处理大映射表中的数据以确定是否满足规则条件
     * 此方法主要负责遍历大映射表状态，计算每个事件字段的累加值，判断是否满足规则条件，并返回相关的处理结果
     *
     * @param ruleConditionMapByEventField 按事件字段分类的规则条件映射表
     * @param ruleCondCombOp 规则条件的组合操作符，用于确定如何组合多个规则条件的结果
     * @return 返回一个Tuple3对象，包含事件结果、最新的Kafka事件DTO和处理器DTO
     * @throws Exception 如果处理过程中发生错误，则抛出异常
     */
    private Tuple3<Boolean, KafkaEventDTO, ProcessorDTO> processBigMap(Map<String, RuleCondDTO> ruleConditionMapByEventField,
                                                                       String ruleCondCombOp) throws Exception {
        // 获取事件与之判断结果
        Map<String, Boolean> eventFieldAndWarnResult = new HashMap<>();
        // 获取事件字段与值之和
        Map<String, Long> eventFiledAndValueSumMap = new HashMap<>();
        // 获取最新的最新的Kafka事件
        KafkaEventDTO latestEventKafkaDTO = null;
        Long maxTimestamp = Long.MIN_VALUE;
        // 遍历 MapState 的所有条目
        for (Map.Entry<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>> entry : bigMapState.entries()) {
            Tuple2<String, Long> key = entry.getKey(); // 获取键，包含 eventField 和关联的时间戳值
            Tuple2<Long, KafkaEventDTO> value = entry.getValue(); // 获取值，包含累加值和 KafkaEventDTO 对象
            Long currentTimestamp = key.f1; // 时间戳
            // 比较当前时间戳是否大于已记录的最大时间戳
            if (currentTimestamp > maxTimestamp) {
                maxTimestamp = currentTimestamp;
                latestEventKafkaDTO = value.f1; // 获取最新的 KafkaEventDTO 对象
            }
            String eventField = key.f0; // Tuple2 的第一个元素作为事件字段
            // 使用 merge 方法高效地累加值
            eventFiledAndValueSumMap.merge(eventField, value.f0, Long::sum);
        }
        // 确保所有规则条件中的事件字段都被包含，如果不存在则设置为 0L
        Set<String> eventFieldSet = ruleConditionMapByEventField.keySet();
        for (String eventField : eventFieldSet) {
            eventFiledAndValueSumMap.putIfAbsent(eventField, 0L);
        }
        // 判断是否触发规则事件阈值
        for (String eventField : eventFieldSet) {
            Long eventValueSum = eventFiledAndValueSumMap.get(eventField);
            RuleCondDTO ruleCondDTO = ruleConditionMapByEventField.get(eventField);
            Long eventThreshold = ruleCondDTO.getThreshold();
            eventFieldAndWarnResult.put(eventField, eventValueSum > eventThreshold);
        }
        boolean eventResult = evaluateEventResults(eventFieldAndWarnResult, ruleCondCombOp);
        // 构建运算机的DTO对象
        ProcessorDTO processorDTO = ProcessorDTO.builder()
                .eventFieldAndValueSumMap(eventFiledAndValueSumMap)
                .build();
        return Tuple3.of(eventResult, latestEventKafkaDTO, processorDTO);
    }


    /**
     * 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMapState中
     */
    private void updateBigMapWithSmallMap(String currentKey, long timestamp) throws Exception {
        // 遍历 smallMapState 的所有条目
        Map<String, Tuple2<Long, KafkaEventDTO>> stringTuple2Map = smallMap.get(currentKey);
        for (Map.Entry<String, Tuple2<Long, KafkaEventDTO>> smallMapEntry : stringTuple2Map.entrySet()) { // 性能优化
            String eventField = smallMapEntry.getKey();
            Tuple2<Long, KafkaEventDTO> tupleValue = smallMapEntry.getValue();

            // 创建新的 Tuple2 作为 bigMapState 的键
            Tuple2<String, Long> tupleKey = Tuple2.of(eventField, timestamp);

            // 将 (eventField, timestamp) 作为键，eventValue 作为值，存入 bigMapState
            bigMapState.put(tupleKey, tupleValue);
        }
        // 当前窗口步长的数据已经添加到窗口中了，清空当前key状态
        smallMap.remove(currentKey); // 性能优化
    }

    /**
     * 清理窗口大小之外的数据
     */
    private void cleanupWindowData(long timestamp, Map<String, RuleCondDTO> ruleConditionMapByEventField) throws Exception {
        // 提前计算每个 eventField 的 windowSize 和 windowThresholdTime
        Map<String, Long> eventFieldToThresholdTime = new HashMap<>();

        for (Map.Entry<String, RuleCondDTO> entry : ruleConditionMapByEventField.entrySet()) {
            String eventField = entry.getKey();
            RuleCondDTO ruleCondDTO = entry.getValue();
            long windowSize = TimeUtil.toMillis(ruleCondDTO.getWindowValue(),
                    TimeUnitEnum.fromEnUnit(ruleCondDTO.getWindowUnit()));
            long windowThresholdTime = timestamp - windowSize;
            eventFieldToThresholdTime.put(eventField, windowThresholdTime);
        }

        // 遍历 bigMapState 的所有条目
        Iterator<Map.Entry<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>>> iterator = bigMapState.entries().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>> stateEntry = iterator.next();
            Tuple2<String, Long> keyTuple = stateEntry.getKey();
            String eventField = keyTuple.f0;
            Long eventTime = keyTuple.f1;

            RuleCondDTO ruleCondDTO = ruleConditionMapByEventField.get(eventField);
            if (Objects.isNull(ruleCondDTO)) {
                log.warn("清理窗口大小之外的数据时，存在规则条件中不存在的 eventField: {}", eventField);
                continue;
            }

            Long windowThresholdTime = eventFieldToThresholdTime.get(eventField);
            if (eventTime <= windowThresholdTime) {
                // 删除过期的条目
                iterator.remove();
            }
        }
    }

    /**
     * 评估事件结果，根据给定的条件操作符返回最终结果。
     *
     * @param eventFieldAndWarnResult 包含事件代码及其对应的警告结果的映射
     * @param conditionOperator       条件操作符，支持 AND 和 OR
     * @return 根据条件操作符计算后的最终结果（true 或 false）
     */
    private boolean evaluateEventResults(Map<String, Boolean> eventFieldAndWarnResult, String conditionOperator) {
        // 检查输入是否为 null 或为空
        if (eventFieldAndWarnResult == null || eventFieldAndWarnResult.isEmpty()) {
            return false;
        }

        // 如果只有一个元素，直接返回该元素的值
        if (eventFieldAndWarnResult.size() == 1) {
            return eventFieldAndWarnResult.values().iterator().next();
        }

        // 确定操作符类型
        RuleCondCombOpEnum opEnum = RuleCondCombOpEnum.fromCode(conditionOperator);

        // 初始化结果，根据操作符类型
        boolean result;
        if (opEnum == RuleCondCombOpEnum.AND) {
            result = true; // 对于 AND，初始值为 true
            for (Boolean eventResult : eventFieldAndWarnResult.values()) {
                if (!eventResult) {
                    return false; // 任何一个 false 都返回 false
                }
            }
        } else if (opEnum == RuleCondCombOpEnum.OR) {
            result = false; // 对于 OR，初始值为 false
            for (Boolean eventResult : eventFieldAndWarnResult.values()) {
                if (eventResult) {
                    return true; // 任何一个 true 都返回 true
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported condition operator: " + conditionOperator);
        }

        return result;
    }

}
