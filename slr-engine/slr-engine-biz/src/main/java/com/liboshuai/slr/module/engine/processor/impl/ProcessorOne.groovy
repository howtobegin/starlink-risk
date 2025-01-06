package com.liboshuai.slr.module.engine.processor.impl


import com.liboshuai.slr.framework.common.constants.RedisKeyConstants
import com.liboshuai.slr.framework.common.enums.CommonStatusEnum
import com.liboshuai.slr.module.engine.dto.*
import com.liboshuai.slr.module.engine.enums.RuleCondCombOpEnum
import com.liboshuai.slr.module.engine.enums.RuleCondTypeEnum
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum
import com.liboshuai.slr.module.engine.framework.exception.BusinessException
import com.liboshuai.slr.module.engine.processor.Processor
import com.liboshuai.slr.module.engine.utils.*
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
/**
 * 运算机one
 */
class ProcessorOne implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ProcessorOne.class)

    /**
     * smallValue（窗口步长）: key为eventField,value为eventValue和最新的EventKafkaDTO
     */
    private MapState<String, Tuple2<Long, KafkaEventDTO>> smallMapState
    /**
     * 记录对应eventField是否已经初始化过（注意不要使用ListState，它查找指定元素的性能很差）
     */
    private MapState<String, Boolean> smallInitMapState
    /**
     * bigValue（窗口大小）: key为eventField，小map的key为时间戳，小map的value为一个一个步长的eventValue累加值和最新的EventKafkaDTO
     */
    private MapState<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapState
    /**
     * 对应 keyCode + keyValue 最近一次预警时间
     */
    private ValueState<Long> lastWarningTimeState

    // 旧状态值
    private MapState<String, Tuple2<Long, KafkaEventDTO>> oldSmallMapState
    private MapState<String, Boolean> oldSmallInitMapState
    private MapState<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> oldBigMapState
    private ValueState<Long> oldLastWarningTimeState

    /**
     * 初始化方法，用于在运行时上下文中注册各种状态
     *
     * @param runtimeContext 运行时上下文，用于访问状态和其它运行时设施
     * @param ruleInfoDTO 规则信息数据传输对象，包含规则特定的元数据
     * @throws Exception 如果初始化过程中发生错误则抛出异常
     */
    @Override
    void init(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) throws Exception {
        String ruleCode = ruleInfoDTO.getRuleCode()
        Long ruleVersion = ruleInfoDTO.getRuleVersion()
        // 状态变量注册使用 ruleCode + ruleVersion 作为后缀，以防止不同规则使用相同的模型导致状态变量数据冲突覆盖
        String smallMapStateName = new StringBuilder("smallMapState_").append(ruleCode).append("_").append(ruleVersion).toString()
        smallMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(
                        smallMapStateName, Types.STRING,
                        Types.TUPLE(Types.LONG, Types.POJO(KafkaEventDTO.class))
                )
        )
        String smallInitMapStateName = new StringBuilder("smallInitMapState_").append(ruleCode).append("_").append(ruleVersion).toString()
        smallInitMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(smallInitMapStateName, Types.STRING, Types.BOOLEAN)
        )
        String bigMapStateName = new StringBuilder("bigMapState_").append(ruleCode).append("_").append(ruleVersion).toString()
        bigMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(bigMapStateName, Types.STRING,
                        Types.MAP(Types.LONG, Types.TUPLE(Types.LONG, Types.POJO(KafkaEventDTO.class))))
        )
        String lastWarningTimeStateName = new StringBuilder("lastWarningTimeState_").append(ruleCode).append("_").append(ruleVersion).toString()
        lastWarningTimeState = runtimeContext.getState(
                new ValueStateDescriptor<>(lastWarningTimeStateName, Types.LONG)
        )
        // 旧状态值
        Long oldRuleVersion = ruleVersion - 1
        oldSmallMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(
                        "smallMapState_${ruleCode}_${oldRuleVersion}", Types.STRING,
                        Types.TUPLE(Types.LONG, Types.POJO(KafkaEventDTO.class))
                )
        )
        oldSmallInitMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("smallInitMapState_${ruleCode}_${oldRuleVersion}", Types.STRING, Types.BOOLEAN)
        )
        oldBigMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("bigMapState_${ruleCode}_${oldRuleVersion}", Types.STRING,
                        Types.MAP(Types.LONG, Types.TUPLE(Types.LONG, Types.POJO(KafkaEventDTO.class))))
        )
        oldLastWarningTimeState = runtimeContext.getState(
                new ValueStateDescriptor<>("lastWarningTimeState_${ruleCode}_${oldRuleVersion}", Types.LONG)
        )
    }

    /**
     * 处理元素事件，根据给定的规则信息和Kafka事件进行处理
     *
     * @param currentEventTimestamp 时间戳，用于处理的时间参考
     * @param ruleInfoDTO 规则信息数据传输对象，包含规则的详细信息
     * @param kafkaEventDTO Kafka事件数据传输对象，包含事件的详细信息
     * @param out 用于输出处理结果的收集器
     * @throws Exception 如果处理过程中遇到任何错误，则抛出异常
     */
    @Override
    void processElement(long currentEventTimestamp, RuleInfoDTO ruleInfoDTO, KafkaEventDTO kafkaEventDTO,
                        Collector<ResultDTO> out) throws Exception {
        if (Objects.isNull(ruleInfoDTO)) {
            throw new BusinessException("运算机 ruleInfoDTO 必须非空")
        }
        if (!Objects.equals(ruleInfoDTO.getRuleStatus(), CommonStatusEnum.ONLINE.getCode())
                && !Objects.equals(ruleInfoDTO.getRuleStatus(), CommonStatusEnum.OFFLINE_PENDING.getCode())) {
            log.warn("加载到运算机池中的规则状态必须为'已上线'或'下线待审核'！规则编号：{}", ruleInfoDTO.getRuleCode())
            return
        }
        // 事件与规则渠道匹配不上，则直接跳过
        if (!Objects.equals(kafkaEventDTO.getChannel(), ruleInfoDTO.getChannel())) {
            return
        }
        // 事件与规则目标匹配不上，则直接跳过
        if (!Objects.equals(kafkaEventDTO.getTargetField(), ruleInfoDTO.getTargetField())) {
            return
        }
        // 获取规则条件
        List<RuleCondDTO> condGroupList = ruleInfoDTO.getRuleCondGroup()
        if (condGroupList == null || condGroupList.isEmpty()) {
            throw new BusinessException("运算机 condGroupList 必须非空")
        }
        // 此模型仅支持条件为周期类型的规则
        for (RuleCondDTO condGroupDTO in condGroupList) {
            String type = condGroupDTO.getCondType()
            if (!Objects.equals(type, RuleCondTypeEnum.PERIODIC.getCode())) {
                log.warn("ProcessorOne 模型仅支持条件为周期类型的规则！规则编号：{}", ruleInfoDTO.getRuleCode())
                return
            }
        }
        // 计算规则条件值
        processRuleCondValue(currentEventTimestamp, ruleInfoDTO, kafkaEventDTO, out)
    }

    /**
     * 处理规则条件值
     *
     * 该方法主要用于处理一组规则条件DTO，通过与Kafka事件DTO进行匹配来更新状态值
     * 如果规则条件跨越历史时间段，则需要从Redis中获取历史事件值，并进行初始化
     *
     * @param currentEventTimestamp 时间戳，用于非跨历史时间段的事件匹配
     * @param ruleCondDtoGroup 规则条件DTO列表
     * @param kafkaEventDTO Kafka事件DTO
     */
    private void processRuleCondValue(long currentEventTimestamp, RuleInfoDTO ruleInfoDTO,
                                      KafkaEventDTO kafkaEventDTO, Collector<ResultDTO> out) {
        List<RuleCondDTO> ruleCondGroup = ruleInfoDTO.getRuleCondGroup()
        for (RuleCondDTO ruleCondDTO : ruleCondGroup) {
            // 事件与规则中的事件编号匹配不上，则直接跳过
            if (!Objects.equals(kafkaEventDTO.getEventField(), ruleCondDTO.getEventField())) {
                // 事件编号匹配不上，则直接跳过
                continue
            }
            // 进行事件属性匹配
            List<RuleEventAttrValueDTO> ruleEventAttrValueGroup = ruleCondDTO.getRuleEventAttrValueGroup()
            boolean eventAttributeMatchResult = matchEventAttribute(ruleEventAttrValueGroup, kafkaEventDTO)
            if (!eventAttributeMatchResult) {
                // 事件属性匹配不上，则直接跳过
                continue
            }
            // 规则状态的key历史记录
            RuleKeyHistoryDTO keyDTO = RuleKeyHistoryDTO.builder()
                    .ruleCode(ruleInfoDTO.getRuleCode())
                    .ruleVersion(ruleInfoDTO.getRuleVersion())
                    .channel(ruleInfoDTO.getChannel())
                    .targetField(kafkaEventDTO.getTargetField())
                    .targetValue(kafkaEventDTO.getTargetValue())
                    .build()
            out.collect(ResultDTO.builder().ruleKeyHistoryDTO(keyDTO).build())
            // 状态值防空
            if (smallMapState.get(kafkaEventDTO.getEventField()) == null) {
                smallMapState.put(kafkaEventDTO.getEventField(), Tuple2.of(0L, kafkaEventDTO))
            }
            if (ruleCondDTO.getCrossHistory()) { //跨历史时间段
                String crossHistoryTimeline = ruleCondDTO.getCrossHistoryTimeline()
                // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，
                // 所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据
                if (kafkaEventDTO.getEventTime()
                        <= DateUtil.convertString2Timestamp(crossHistoryTimeline)) {
                    continue
                }
                // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，
                // 所以检查当前值是否已经通过redis初始化后，防止重复初始化
                if (!smallInitMapState.contains(kafkaEventDTO.getEventField())) {
                    // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值
                    // （注意：Groovy字符串拼接的方式很麻烦，故使用StringBuilder）
                    String redisKey = buildRedisKey(ruleCondDTO)
                    String redisHashKey = buildRedisHashKey(kafkaEventDTO)
                    // 注意：因为上面获取历史缓存数据时，使用的是 <= 所以 redis 存储值时查询 doris 要包含历史截至时间点
                    String initValue = RedisUtil.hget(redisKey, redisHashKey)
                    RedisUtil.hdel(redisKey, redisHashKey)
                    if (StringUtils.isNullOrWhitespaceOnly(initValue)) {
                        throw new BusinessException(
                                StringUtil.format("从redis获取初始值必须非空, redisKey:{}, hashKey: {}", redisKey, redisHashKey)
                        )
                    }
                    smallMapState.put(kafkaEventDTO.getEventField(), Tuple2.of(Long.parseLong(initValue), kafkaEventDTO))
                    smallInitMapState.put(kafkaEventDTO.getEventField(), true)
                }
                // 从redis初始化值后，正常处理数据
                Tuple2<Long, KafkaEventDTO> currentTuple = smallMapState.get(kafkaEventDTO.getEventField())
                Long newValue = currentTuple.f0 + Long.parseLong(kafkaEventDTO.getEventValue())
                smallMapState.put(kafkaEventDTO.getEventField(), Tuple2.of(newValue, kafkaEventDTO))
            } else { // 非跨历史时间段
                // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据
                if (kafkaEventDTO.getEventTime() != currentEventTimestamp) {
                    continue
                }
                Tuple2<Long, KafkaEventDTO> currentTuple = smallMapState.get(kafkaEventDTO.getEventField())
                Long newValue = currentTuple.f0 + Long.parseLong(kafkaEventDTO.getEventValue())
                smallMapState.put(kafkaEventDTO.getEventField(), Tuple2.of(newValue, kafkaEventDTO))
            }
        }
    }


    /**
     * 构建Redis的哈希键
     */
    private String buildRedisHashKey(KafkaEventDTO kafkaEventDTO) {
        String targetField = kafkaEventDTO.getTargetField()
        String targetValue = kafkaEventDTO.getTargetValue()
        return new StringBuilder()
                .append(targetField)
                .append(RedisKeyConstants.REDIS_KEY_SPLIT)
                .append(targetValue).toString()
    }

    /**
     * 构建Redis的key
     */
    private String buildRedisKey(RuleCondDTO ruleCondDTO) {
        return new StringBuilder()
                .append(RedisKeyConstants.REDIS_KEY_PREFIX)
                .append(RedisKeyConstants.REDIS_KEY_SPLIT)
                .append(RedisKeyConstants.DORIS_EVENT_HISTORY_VALUE)
                .append(RedisKeyConstants.REDIS_KEY_SPLIT)
                .append(ruleCondDTO.getRuleCode())
                .append(RedisKeyConstants.REDIS_KEY_SPLIT)
                .append(ruleCondDTO.getEventField())
    }

    /**
     * 匹配规则事件属性与Kafka事件属性是否符合
     *
     * 此方法的目的是为了验证给定的Kafka事件是否满足规则事件中定义的所有属性条件
     * 它通过比较规则事件属性和Kafka事件属性来确定两者是否匹配
     *
     * @param ruleEventDTO 规则事件DTO，包含规则事件的详细信息，包括事件属性
     * @param kafkaEventDTO Kafka事件DTO，包含Kafka事件的详细信息，包括事件属性
     * @return boolean 如果Kafka事件属性与规则事件属性完全匹配，则返回true；否则返回false
     */
    private boolean matchEventAttribute(List<RuleEventAttrValueDTO> ruleEventAttrValueGroup, KafkaEventDTO kafkaEventDTO) {
        if (CollectionUtil.isEmptyOrContainsNulls(ruleEventAttrValueGroup)) {
            // 规则中不包含事件属性相关的配置，则表明不需要进行事件属性匹配，直接跳过即可
            return true
        }
        // 逐一便利验证事件属性
        for (RuleEventAttrValueDTO ruleEventAttrValueDTO in ruleEventAttrValueGroup) {
            String attrValue = ruleEventAttrValueDTO.getAttrValue()
            if (StringUtils.isNullOrWhitespaceOnly(attrValue)) {
                // 规则中不包含事件属性值相关的配置，则表明不需要进行事件属性值匹配，直接跳过即可
                continue
            }
            String attrField = ruleEventAttrValueDTO.getAttrField()
            Map<String, String> kafkaEventAttrMap = kafkaEventDTO.getEventAttrMap()
            if (CollectionUtil.isEmpty(kafkaEventAttrMap)) {
                // 规则包含事件属性配置，但是kafka数据事件属性map为空，故直接判定为不符合规则要求
                log.warn("规则包含事件属性配置，但是kafka数据事件属性map为空，故直接判定为不符合规则要求！" +
                        "规则事件属性信息:{}, kafka事件信息:{}", ruleEventAttrValueDTO, kafkaEventDTO)
                return false
            }
            if (!kafkaEventAttrMap.containsKey(attrField)) {
                // kafka事件属性不包含规则中事件属性，则表明不符合匹配
                log.warn("kafka数据事件属性map并不包含规则配置的事件属性Field，故直接判定为不符合规则要求！" +
                        "规则事件属性信息:{}, kafka事件信息:{}", ruleEventAttrValueDTO, kafkaEventDTO)
                return false
            }
            String kafkaEventAttributeValue = kafkaEventAttrMap.get(attrField)
            if (Objects.isNull(kafkaEventAttributeValue)) {
                // kafka事件中对于规则中事件属性值为空，则表明不符合匹配
                return false
            }
            // 比较kafka中属性值与规则中属性值
            boolean isMatch = RuleEventAttrCompUtil.compareValues(ruleEventAttrValueDTO, kafkaEventDTO)
            if (!isMatch) {
                // kafka事件属性值与规则事件属性值不相等，则表明不符合匹配
                return false
            }
        }
        // 所有事件属性都匹配，则表明符合匹配
        return true
    }

    /**
     * 定时器触发时执行的方法
     *
     * @param timestamp 时间戳，表示当前时间
     * @param ruleInfoDTO 规则信息DTO，包含规则相关数据
     * @param out 输出收集器，用于收集和输出预警信息
     * @throws Exception 可能抛出的异常
     */
    @Override
    boolean onTimer(long timestamp, String currentKey, RuleInfoDTO ruleInfoDTO, Collector<ResultDTO> out) throws Exception {
//        logOldState()
        if (Objects.isNull(ruleInfoDTO)) {
            throw new BusinessException("运算机 ruleInfoDTO 必须非空")
        }
        // 获取规则条件
        List<RuleCondDTO> groupGroup = ruleInfoDTO.getRuleCondGroup()
        if (groupGroup == null || groupGroup.isEmpty()) {
            throw new BusinessException("运算机 groupGroup 必须非空")
        }
        // 将规则条件根据事件编号存储到map中，方便后续操作
        Map<String, RuleCondDTO> ruleConditionMapByEventField = new HashMap<>()
        for (RuleCondDTO ruleCondDTO : groupGroup) {
            ruleConditionMapByEventField.put(ruleCondDTO.getEventField(), ruleCondDTO)
        }
        // 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMapState中
        updateBigMapWithSmallMap(timestamp)
        // 清理窗口大小之外的数据
        cleanupWindowData(timestamp, ruleConditionMapByEventField)
        // 判断是否触发规则事件阈值
        boolean eventResult = evaluateEventThresholds(ruleConditionMapByEventField, ruleInfoDTO)
        // 根据规则中事件条件表达式组合判断事件结果 与预警频率 判断否是触发预警
        if (lastWarningTimeState.value() == null) {
            lastWarningTimeState.update(0L)
        }
        // 获取预警间隔时间，单位为毫秒
        long alertInterval = TimeUtil.toMillis(
                ruleInfoDTO.getAlertIntervalValue(), TimeUnitEnum.fromEnUnit(ruleInfoDTO.getAlertIntervalUnit())
        )
        // 触发结果为true，且当前时间减去上次预警时间大于预警间隔时间，则进行预警
        if (eventResult && (timestamp - lastWarningTimeState.value() >= alertInterval)) {
            lastWarningTimeState.update(timestamp)
            // 进行预警信息拼接组合
            String finalWarnMessage = TemplateUtil.replacePlaceholders(
                    ruleInfoDTO.getAlertMessage(),
                    ruleInfoDTO,
                    getLatestEventKafkaDto(),
                    getProcessorDto()
            )
            AlertMessageDTO alertMessageDTO = AlertMessageDTO.builder()
                    .channel(ruleInfoDTO.getChannel())
                    .ruleCode(ruleInfoDTO.getRuleCode())
                    .alertMessage(finalWarnMessage)
                    .alertTime(DateUtil.convertTimestamp2LocalDateTime(System.currentTimeMillis()))
                    .build()
            log.warn("当前Key: {}, 最终推送的预警信息内容：{}", currentKey, alertMessageDTO)
            ResultDTO resultDTO = ResultDTO.builder().alertMessageDTO(alertMessageDTO).build()
            out.collect(resultDTO)
        }
        // 调试使用，待删除
        logBigMapState(currentKey, ruleInfoDTO.getRuleCode(), ruleConditionMapByEventField.keySet(), bigMapState)
        return hasActiveEvents()
    }

    /**
     * 检查是否存在活跃的事件
     * 该方法用于遍历一个大的状态映射，以确定其中是否包含活跃的Kafka事件
     *
     * @return boolean - 如果存在活跃的事件，则返回true；否则返回false
     */
    private boolean hasActiveEvents() {
        boolean result = false
        for (Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapEntry : bigMapState.entries()) {
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueMap = bigMapEntry.getValue()
            if (!CollectionUtil.isEmpty(timestampAndEventValueMap)) {
                result = true
                break
            }
        }
        result
    }

    /**
     * 判断是否触发规则事件阈值。
     *
     * <p>此方法遍历 `bigMapState` 中的所有事件代码及其对应的时间戳和事件值累加，对每个事件代码
     * 的累加值与预定义的阈值进行比较。如果某个事件代码的累加值超过其阈值，则在结果映射中记录为 `true`。
     * 最后，根据 `ruleInfoDTO` 中指定的组合条件操作符（如 AND/OR）评估所有事件代码的结果，从而确定
     * 是否整体满足触发规则的条件。
     *
     * @param ruleConditionMapByEventField 按事件代码分组的规则条件映射，每个事件代码对应一个 `RuleConditionDTO`
     * @param ruleInfoDTO 规则信息数据传输对象，包含组合条件操作符等规则配置
     * @return 如果根据组合条件操作符评估后满足规则阈值条件，返回 `true`；否则返回 `false`
     * @throws Exception 在评估过程中发生任何异常时抛出
     */
    private boolean evaluateEventThresholds(Map<String, RuleCondDTO> ruleConditionMapByEventField,
                                            RuleInfoDTO ruleInfoDTO) throws Exception {
        Map<String, Boolean> eventFieldAndWarnResult = new HashMap<>()
        for (Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapEntry : bigMapState.entries()) {
            String eventField = bigMapEntry.getKey()
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()
            long eventValueSum = timestampAndEventValueKafkaDtoMap.values().stream()
                    .map(o -> o.f0)
                    .mapToLong(Long::longValue)
                    .sum()
            RuleCondDTO ruleCondDTO = ruleConditionMapByEventField.get(eventField)
            if (ruleCondDTO != null) {
                Long eventThreshold = ruleCondDTO.getThreshold()
                eventFieldAndWarnResult.put(eventField, eventValueSum > eventThreshold)
            }
        }
        boolean eventResult = evaluateEventResults(eventFieldAndWarnResult, ruleInfoDTO.getRuleCondCombOp())
        return eventResult
    }

    /**
     * 聚合 bigMapState 中的事件值并构建 ProcessorDTO 对象。
     *
     * <p>该方法遍历 bigMapState，其中每个键为 eventField，值为一个包含时间戳和对应
     * Tuple2<Long, EventKafkaDTO> 的映射。对于每个 eventFiled，方法会将所有时间戳
     * 下的 eventValue（Tuple2 中的第一个值）进行累加，生成一个 eventField 与其
     * 累加值的映射。最后，基于这些聚合结果构建并返回一个包含 eventFiledAndValueSumMap 的 ProcessorDTO 对象。</p>
     *
     * @return 包含每个 eventFiled 对应 eventValue 累加值的 ProcessorDTO 对象
     * @throws Exception 如果在处理过程中发生错误
     */
    private ProcessorDTO getProcessorDto() throws Exception {
        Map<String, Long> eventFiledAndValueSumMap = new HashMap<>()
        for (Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapEntry : bigMapState.entries()) {
            String eventField = bigMapEntry.getKey()
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()
            long eventValueSum = timestampAndEventValueKafkaDtoMap.values().stream()
                    .map(o -> o.f0)
                    .mapToLong(Long::longValue)
                    .sum()
            eventFiledAndValueSumMap.put(eventField, eventValueSum)
        }
        ProcessorDTO processorDTO = ProcessorDTO.builder()
                .eventFieldAndValueSumMap(eventFiledAndValueSumMap)
                .build()
        return processorDTO
    }


    /**
     * 从所有事件条件累积的值流中检索最新的 Kafka 事件数据。
     *
     * <p>此方法遍历 `bigMapState` 中存储的所有事件数据，查找具有最大时间戳的 `EventKafkaDTO` 对象，
     * 并返回该最新的事件数据。
     *
     * @return 最新的 {@link KafkaEventDTO} 对象，如果没有事件数据则返回 {@code null}
     * @throws Exception 如果在遍历过程中发生异常
     */
    private KafkaEventDTO getLatestEventKafkaDto() throws Exception {
        // 初始化变量，用于存储最新的 EventKafkaDTO 和对应的最大时间戳
        KafkaEventDTO latestEventKafkaDTO = null
        Long maxTimestamp = Long.MIN_VALUE

        // 遍历 bigMapState 中的每一个大键（eventField）及其对应的内部映射
        for (Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapEntry : bigMapState.entries()) {
            // 获取当前 eventField 对应的时间戳与事件数据的映射
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()

            // 获取当前映射中的所有条目（时间戳与事件数据对）
            Set<Map.Entry<Long, Tuple2<Long, KafkaEventDTO>>> entrySet = timestampAndEventValueKafkaDtoMap.entrySet()

            // 遍历当前 eventField 下的所有时间戳和事件数据对
            for (Map.Entry<Long, Tuple2<Long, KafkaEventDTO>> entry : entrySet) {
                Long currentTimestamp = entry.getKey() // 当前条目的时间戳
                Tuple2<Long, KafkaEventDTO> value = entry.getValue() // 包含累加值和事件数据的元组

                // 如果当前时间戳大于已记录的最大时间戳，则更新最大时间戳和最新的事件数据
                if (currentTimestamp > maxTimestamp) {
                    maxTimestamp = currentTimestamp
                    latestEventKafkaDTO = value.f1 // 获取元组中的 EventKafkaDTO 对象
                }
            }
        }

        // 返回找到的最新的 EventKafkaDTO 对象
        return latestEventKafkaDTO
    }


    /**
     * 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMapState中
     */
    private void updateBigMapWithSmallMap(long timestamp) throws Exception {
        for (Map.Entry<String, Tuple2<Long, KafkaEventDTO>> smallMapEntry : smallMapState.entries()) {
            String eventField = smallMapEntry.getKey()
            Tuple2<Long, KafkaEventDTO> eventValueAndKafkaDtoTuple2 = smallMapEntry.getValue()
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueMap = bigMapState.get(eventField)
            if (timestampAndEventValueMap == null || timestampAndEventValueMap.isEmpty()) {
                timestampAndEventValueMap = new HashMap<>()
            }
            timestampAndEventValueMap.put(timestamp, eventValueAndKafkaDtoTuple2)
            bigMapState.put(eventField, timestampAndEventValueMap)
        }
        // 当前窗口步长的数据已经添加到窗口中了，清空状态
        smallMapState.clear()
    }

    /**
     * 清理窗口大小之外的数据
     */
    private void cleanupWindowData(long timestamp, Map<String, RuleCondDTO> ruleConditionMapByEventField) throws Exception {
        for (Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapEntry : bigMapState.entries()) {
            String eventField = bigMapEntry.getKey()
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueMap = bigMapEntry.getValue()
            RuleCondDTO ruleCondDTO = ruleConditionMapByEventField.get(eventField)
            if (Objects.isNull(ruleCondDTO)) {
                log.warn("清理窗口大小之外的数据时，存在规则条件中不存在的数据")
                continue
            }
            long windowSize = TimeUtil.toMillis(ruleCondDTO.getWindowValue(), TimeUnitEnum.fromEnUnit(ruleCondDTO.getWindowUnit()))
            long windowThresholdTime = timestamp - windowSize
            Iterator<Map.Entry<Long, Tuple2<Long, KafkaEventDTO>>> iterator = timestampAndEventValueMap.entrySet().iterator()
            while (iterator.hasNext()) {
                Map.Entry<Long, Tuple2<Long, KafkaEventDTO>> next = iterator.next()
                Long time = next.getKey()
                if (time <= windowThresholdTime) {
                    iterator.remove()
                }
            }
            bigMapState.put(eventField, timestampAndEventValueMap)
        }
    }

    /**
     * 打印老状态值
     */
    private void logOldState() throws Exception {
        Map<String, Tuple2<Long, KafkaEventDTO>> oldSmallMap = new HashMap<>()
        Iterator<Map.Entry<String, Tuple2<Long, KafkaEventDTO>>> oldSmallMapIterator = oldSmallMapState.iterator()
        while (oldSmallMapIterator.hasNext()) {
            Map.Entry<String, Tuple2<Long, KafkaEventDTO>> next = oldSmallMapIterator.next()
            oldSmallMap.put(next.getKey(), next.getValue())
        }

        Map<String, Boolean> oldSmallInitMap = new HashMap<>()
        Iterator<Map.Entry<String, Boolean>> oldSmallInitMapIterator = oldSmallInitMapState.iterator()
        while (oldSmallInitMapIterator.hasNext()) {
            Map.Entry<String, Boolean> next = oldSmallInitMapIterator.next()
            oldSmallInitMap.put(next.getKey(), next.getValue())
        }

        Map<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> oldBigMap = new HashMap<>()
        Iterator<Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>>> oldBigMapStateIterator = oldBigMapState.iterator()
        while (oldBigMapStateIterator.hasNext()) {
            Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> next = oldBigMapStateIterator.next()
            oldBigMap.put(next.getKey(), next.getValue())
        }

        Long oldLastWarningTime = oldLastWarningTimeState.value()

        log.warn("========================================旧状态值========================================")
        log.warn("oldSmallInitMap: {}", JsonUtil.toJsonString(oldSmallInitMap))
        log.warn("oldSmallMap: {}", JsonUtil.toJsonString(oldSmallMap))
        log.warn("oldBigMap: {}", JsonUtil.toJsonString(oldBigMap))
        log.warn("========================================旧状态值========================================")
    }

    /**
     * 日志打印
     */
    private void logBigMapState(String currentKey, Long ruleCode, Set<String> eventFieldList, MapState<String,
            Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapState) throws Exception {
        Map<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMap = new HashMap<>()
        Iterator<Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>>> iterator = bigMapState.iterator()
        while (iterator.hasNext()) {
            Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> next = iterator.next()
            bigMap.put(next.getKey(), next.getValue())
        }
        log.warn("ProcessorOne对象onTimer方法结束 currentKey={}, ruleCode={}, eventFieldList={}, bigMapState={}",
                currentKey, ruleCode, JsonUtil.toJsonString(eventFieldList), JsonUtil.toJsonString(bigMap))
    }

    /**
     * 评估事件结果，根据给定的条件操作符返回最终结果。
     *
     * @param eventFieldAndWarnResult 包含事件代码及其对应的警告结果的映射
     * @param conditionOperator 条件操作符，支持 AND 和 OR
     * @return 根据条件操作符计算后的最终结果（true 或 false）
     */
    boolean evaluateEventResults(Map<String, Boolean> eventFieldAndWarnResult, String conditionOperator) {
        // 检查输入是否为 null 或为空
        if (eventFieldAndWarnResult == null || eventFieldAndWarnResult.isEmpty()) {
            return false
        }

        // 如果只有一个元素，直接返回该元素的值
        if (eventFieldAndWarnResult.size() == 1) {
            return eventFieldAndWarnResult.values().iterator().next()
        }

        // 确定操作符类型
        RuleCondCombOpEnum opEnum = RuleCondCombOpEnum.fromCode(conditionOperator)

        // 初始化结果，根据操作符类型
        boolean result
        if (opEnum == RuleCondCombOpEnum.AND) {
            result = true // 对于 AND，初始值为 true
            for (Boolean eventResult : eventFieldAndWarnResult.values()) {
                if (!eventResult) {
                    return false // 任何一个 false 都返回 false
                }
            }
        } else if (opEnum == RuleCondCombOpEnum.OR) {
            result = false // 对于 OR，初始值为 false
            for (Boolean eventResult : eventFieldAndWarnResult.values()) {
                if (eventResult) {
                    return true // 任何一个 true 都返回 true
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported condition operator: " + conditionOperator)
        }

        return result
    }

}
