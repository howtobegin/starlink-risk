package com.liboshuai.starlink.slr.engine.processor.impl

import com.liboshuai.starlink.slr.engine.api.constants.GlobalConstants
import com.liboshuai.starlink.slr.engine.api.constants.RedisKeyConstants
import com.liboshuai.starlink.slr.engine.api.dto.*
import com.liboshuai.starlink.slr.engine.api.enums.RuleCondCombOpEnum
import com.liboshuai.starlink.slr.engine.api.enums.RuleCondTypeEnum
import com.liboshuai.starlink.slr.engine.api.enums.RuleStatusEnum
import com.liboshuai.starlink.slr.engine.api.enums.TimeUnitEnum
import com.liboshuai.starlink.slr.engine.api.util.TemplatePlaceholderUtil
import com.liboshuai.starlink.slr.engine.exception.BusinessException
import com.liboshuai.starlink.slr.engine.processor.Processor
import com.liboshuai.starlink.slr.engine.utils.*
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
     * smallValue（窗口步长）: key为eventCode,value为eventValue和最新的EventKafkaDTO
     */
    private MapState<String, Tuple2<Long, KafkaEventDTO>> smallMapState

    /**
     * 记录对应eventCode是否已经初始化过（注意不要使用ListState，它查找指定元素的性能很差）
     */
    private MapState<String, Boolean> smallInitMapState

    /**
     * bigValue（窗口大小）: key为eventCode，小map的key为时间戳，小map的value为一个一个步长的eventValue累加值和最新的EventKafkaDTO
     */
    private MapState<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapState

    /**
     * 最近一次预警时间
     */
    private ValueState<Long> lastWarningTimeState

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
        // 状态变量注册使用 ruleCode 作为后缀，以防止不同规则使用相同的模型导致状态变量数据冲突覆盖
        smallMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(
                        "smallMapState_${ruleCode}", Types.STRING,
                        Types.TUPLE(Types.LONG, Types.POJO(KafkaEventDTO.class))
                )
        )
        smallInitMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("smallInitMapState_${ruleCode}", Types.STRING, Types.BOOLEAN)
        )
        bigMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>("bigMapState_${ruleCode}", Types.STRING,
                        Types.MAP(Types.LONG, Types.TUPLE(Types.LONG, Types.POJO(KafkaEventDTO.class))))
        )
        lastWarningTimeState = runtimeContext.getState(
                new ValueStateDescriptor<>("lastWarningTimeState_${ruleCode}", Types.LONG)
        )
    }

    /**
     * 处理元素事件，根据给定的规则信息和Kafka事件进行处理
     *
     * @param timestamp 时间戳，用于处理的时间参考
     * @param ruleInfoDTO 规则信息数据传输对象，包含规则的详细信息
     * @param kafkaEventDTO Kafka事件数据传输对象，包含事件的详细信息
     * @param out 用于输出处理结果的收集器
     * @throws Exception 如果处理过程中遇到任何错误，则抛出异常
     */
    @Override
    void processElement(long timestamp, RuleInfoDTO ruleInfoDTO, KafkaEventDTO kafkaEventDTO, Collector<String> out) throws Exception {
        if (Objects.isNull(ruleInfoDTO)) {
            throw new BusinessException("运算机 ruleInfoDTO 必须非空")
        }
        if (!Objects.equals(ruleInfoDTO.getRuleStatus(), RuleStatusEnum.ONLINE.getCode())
                && !Objects.equals(ruleInfoDTO.getRuleStatus(), RuleStatusEnum.OFFLINE_PENDING.getCode())) {
            log.warn("加载到运算机池中的规则状态必须为'已上线'或'下线待审核'！规则编号：{}", ruleInfoDTO.getRuleCode())
            return
        }
        // 事件与规则渠道匹配不上，则直接跳过
        String eventKafkaDTOChannel = kafkaEventDTO.getChannel()
        String ruleInfoChannel = ruleInfoDTO.getChannel()
        if (!Objects.equals(eventKafkaDTOChannel, ruleInfoChannel)) {
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
        processRuleCondValue(condGroupList, kafkaEventDTO, timestamp)
    }

    /**
     * 处理规则条件值
     *
     * 该方法主要用于处理一组规则条件DTO，通过与Kafka事件DTO进行匹配来更新状态值
     * 如果规则条件跨越历史时间段，则需要从Redis中获取历史事件值，并进行初始化
     *
     * @param ruleCondDtoGroup 规则条件DTO列表
     * @param kafkaEventDTO Kafka事件DTO
     * @param timestamp 时间戳，用于非跨历史时间段的事件匹配
     */
    private void processRuleCondValue(List<RuleCondDTO> ruleCondDtoGroup, KafkaEventDTO kafkaEventDTO, long timestamp) {
        for (RuleCondDTO ruleCondDTO : ruleCondDtoGroup) {
            // 进行事件编号匹配
            if (!Objects.equals(kafkaEventDTO.getEventCode(), ruleCondDTO.getEventCode())) {
                // 事件编号匹配不上，则直接跳过
                continue
            }
            // 进行事件属性匹配
            RuleEventDTO ruleEventDTO = ruleCondDTO.getRuleEventDTO()
            if (Objects.isNull(ruleEventDTO)) {
                throw new BusinessException("事件信息 ruleEventDTO 不能为空")
            }
            boolean eventAttributeMatchResult = matchEventAttribute(ruleEventDTO, kafkaEventDTO)
            if (!eventAttributeMatchResult) {
                // 事件属性匹配不上，则直接跳过
                continue
            }
            // 状态值防空
            if (smallMapState.get(kafkaEventDTO.getEventCode()) == null) {
                smallMapState.put(kafkaEventDTO.getEventCode(), Tuple2.of(0L, kafkaEventDTO))
            }
            if (ruleCondDTO.getCrossHistory()) { //跨历史时间段
                String crossHistoryTimeline = ruleCondDTO.getCrossHistoryTimeline()
                // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，
                // 所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据
                if (kafkaEventDTO.getTimestamp() <= DateUtil.convertString2Timestamp(crossHistoryTimeline)) {
                    continue
                }
                // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，
                // 所以检查当前值是否已经通过redis初始化后，防止重复初始化
                if (!smallInitMapState.contains(kafkaEventDTO.getEventCode())) {
                    // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值
                    // （注意：Groovy字符串拼接的方式很麻烦，故使用StringBuilder）
                    String redisKey = buildRedisKey(ruleCondDTO)
                    String redisHashKey = buildRedisHashKey(kafkaEventDTO)
                    // 注意：因为上面获取历史缓存数据时，使用的是 <= 所以 redis 存储值时查询 doris 要包含历史截至时间点
                    String initValue = RedisUtil.hget(redisKey, redisHashKey)
                    if (StringUtils.isNullOrWhitespaceOnly(initValue)) {
                        throw new BusinessException(
                                StringUtil.format("从redis获取初始值必须非空, redisKey:{}, hashKey: {}", redisKey, redisHashKey)
                        )
                    }
                    smallMapState.put(kafkaEventDTO.getEventCode(), Tuple2.of(Long.parseLong(initValue), kafkaEventDTO))
                    smallInitMapState.put(kafkaEventDTO.getEventCode(), true)
                }
                // 从redis初始化值后，正常处理数据
                Tuple2<Long, KafkaEventDTO> currentTuple = smallMapState.get(kafkaEventDTO.getEventCode())
                Long newValue = currentTuple.f0 + Long.parseLong(kafkaEventDTO.getEventValue())
                smallMapState.put(kafkaEventDTO.getEventCode(), Tuple2.of(newValue, kafkaEventDTO))
            } else { // 非跨历史时间段
                // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据
                if (kafkaEventDTO.getTimestamp() != timestamp) {
                    continue
                }
                Tuple2<Long, KafkaEventDTO> currentTuple = smallMapState.get(kafkaEventDTO.getEventCode())
                Long newValue = currentTuple.f0 + Long.parseLong(kafkaEventDTO.getEventValue())
                smallMapState.put(kafkaEventDTO.getEventCode(), Tuple2.of(newValue, kafkaEventDTO))
            }
        }
    }


    /**
     * 构建Redis的哈希键
     */
    private String buildRedisHashKey(KafkaEventDTO kafkaEventDTO) {
        String keyCode = kafkaEventDTO.getKeyCode()
        String keyValue = kafkaEventDTO.getKeyValue()
        return new StringBuilder()
                .append(keyCode)
                .append(GlobalConstants.REDIS_KEY_SEPARATOR)
                .append(keyValue).toString()
    }

    /**
     * 构建Redis的key
     */
    private String buildRedisKey(RuleCondDTO ruleCondDTO) {
        return new StringBuilder()
                .append(GlobalConstants.SYSTEM_NAME)
                .append(GlobalConstants.REDIS_KEY_SEPARATOR)
                .append(RedisKeyConstants.DORIS)
                .append(GlobalConstants.REDIS_KEY_SEPARATOR)
                .append(ruleCondDTO.getRuleCode())
                .append(GlobalConstants.REDIS_KEY_SEPARATOR)
                .append(ruleCondDTO.getEventCode())
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
    private boolean matchEventAttribute(RuleEventDTO ruleEventDTO, KafkaEventDTO kafkaEventDTO) {
        List<RuleEventAttrDTO> ruleEventAttributeDTOList = ruleEventDTO.getRuleEventAttributeGroup()
        if (CollectionUtil.isEmptyOrContainsNulls(ruleEventAttributeDTOList)) {
            // 规则中不包含事件属性相关的配置，则表明不需要进行事件属性匹配，直接跳过即可
            return true
        }
        // 逐一便利验证事件属性
        for (RuleEventAttrDTO ruleEventAttributeDTO in ruleEventAttributeDTOList) {
            String ruleAttributeKey = ruleEventAttributeDTO.getAttributeKey()
            Map<String, String> kafkaEventAttributeMap = kafkaEventDTO.getEventAttribute()
            if (CollectionUtil.isEmpty(kafkaEventAttributeMap)) {
                // 规则中包含事件属性相关的配置，但是kafka事件属性为空，则表明不符合匹配
                return false
            }
            if (!kafkaEventAttributeMap.containsKey(ruleAttributeKey)) {
                // kafka事件属性不包含规则中事件属性，则表明不符合匹配
                return false
            }
            String kafkaEventAttributeValue = kafkaEventAttributeMap.get(ruleAttributeKey)
            if (Objects.isNull(kafkaEventAttributeValue)) {
                // kafka事件中对于规则中事件属性值为空，则表明不符合匹配
                return false
            }
            String ruleEventAttributeValue = ruleEventAttributeDTO.getAttributeValue()
            if (Objects.isNull(ruleEventAttributeValue)) {
                throw new BusinessException("规则事件属性值必须非空")
            }
            // 比较kafka中属性值与规则中属性值
            boolean isMatch = RuleEventAttrCompUtil.compareValues(
                    kafkaEventAttributeValue,
                    ruleEventAttributeValue,
                    ruleEventAttributeDTO.getAttributeType(),
                    ruleEventAttributeDTO.getAttributeOp()
            )
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
    void onTimer(long timestamp, RuleInfoDTO ruleInfoDTO, Collector<String> out) throws Exception {
        if (Objects.isNull(ruleInfoDTO)) {
            throw new BusinessException("运算机 ruleInfoDTO 必须非空")
        }
        // 获取规则条件
        List<RuleCondDTO> groupGroup = ruleInfoDTO.getRuleCondGroup()
        if (groupGroup == null || groupGroup.isEmpty()) {
            throw new BusinessException("运算机 groupGroup 必须非空")
        }
        // 将规则条件根据事件编号存储到map中，方便后续操作
        Map<String, RuleCondDTO> ruleConditionMapByEventCode = new HashMap<>()
        for (RuleCondDTO ruleCondDTO : groupGroup) {
            ruleConditionMapByEventCode.put(ruleCondDTO.getEventCode(), ruleCondDTO)
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
        // 获取预警间隔时间，单位为毫秒
        long alertInterval = TimeUtil.toMillis(
                ruleInfoDTO.getAlertIntervalValue(), TimeUnitEnum.fromEnUnit(ruleInfoDTO.getAlertIntervalUnit())
        )
        // 触发结果为true，且当前时间减去上次预警时间大于预警间隔时间，则进行预警
        if (eventResult && (timestamp - lastWarningTimeState.value() >= alertInterval)) {
            lastWarningTimeState.update(timestamp)
            // 进行预警信息拼接组合
            String finalWarnMessage = TemplatePlaceholderUtil.replacePlaceholders(
                    ruleInfoDTO.getAlertMessage(),
                    ruleInfoDTO,
                    getLatestEventKafkaDto(),
                    getProcessorDto()
            )
            log.warn("最终推送的预警信息内容：{}", finalWarnMessage)
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
    private boolean evaluateEventThresholds(Map<String, RuleCondDTO> ruleConditionMapByEventCode,
                                            RuleInfoDTO ruleInfoDTO) throws Exception {
        Map<String, Boolean> eventCodeAndWarnResult = new HashMap<>()
        for (Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapEntry : bigMapState.entries()) {
            String eventCode = bigMapEntry.getKey()
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()
            long eventValueSum = timestampAndEventValueKafkaDtoMap.values().stream()
                    .map(o -> o.f0)
                    .mapToLong(Long::longValue)
                    .sum()
            RuleCondDTO ruleCondDTO = ruleConditionMapByEventCode.get(eventCode)
            if (ruleCondDTO != null) {
                Long eventThreshold = ruleCondDTO.getThreshold()
                eventCodeAndWarnResult.put(eventCode, eventValueSum > eventThreshold)
            }
        }
        boolean eventResult = evaluateEventResults(eventCodeAndWarnResult, ruleInfoDTO.getRuleCondCombOp())
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
        for (Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapEntry : bigMapState.entries()) {
            String eventCode = bigMapEntry.getKey()
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()
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
     * @return 最新的 {@link KafkaEventDTO} 对象，如果没有事件数据则返回 {@code null}
     * @throws Exception 如果在遍历过程中发生异常
     */
    private KafkaEventDTO getLatestEventKafkaDto() throws Exception {
        // 初始化变量，用于存储最新的 EventKafkaDTO 和对应的最大时间戳
        KafkaEventDTO latestEventKafkaDTO = null
        Long maxTimestamp = Long.MIN_VALUE

        // 遍历 bigMapState 中的每一个大键（eventCode）及其对应的内部映射
        for (Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapEntry : bigMapState.entries()) {
            // 获取当前 eventCode 对应的时间戳与事件数据的映射
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()

            // 获取当前映射中的所有条目（时间戳与事件数据对）
            Set<Map.Entry<Long, Tuple2<Long, KafkaEventDTO>>> entrySet = timestampAndEventValueKafkaDtoMap.entrySet()

            // 遍历当前 eventCode 下的所有时间戳和事件数据对
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
            String eventCode = smallMapEntry.getKey()
            Tuple2<Long, KafkaEventDTO> eventValueAndKafkaDtoTuple2 = smallMapEntry.getValue()
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueMap = bigMapState.get(eventCode)
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
    private void cleanupWindowData(long timestamp, Map<String, RuleCondDTO> ruleConditionMapByEventCode) throws Exception {
        for (Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapEntry : bigMapState.entries()) {
            String eventCode = bigMapEntry.getKey()
            Map<Long, Tuple2<Long, KafkaEventDTO>> timestampAndEventValueMap = bigMapEntry.getValue()
            RuleCondDTO ruleCondDTO = ruleConditionMapByEventCode.get(eventCode)
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
            bigMapState.put(eventCode, timestampAndEventValueMap)
        }
    }

    private void logBigMapState(String ruleCode, Set<String> eventCodeList, String keyCode, MapState<String,
            Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMapState) throws Exception {
        Map<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> bigMap = new HashMap<>()
        Iterator<Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>>> iterator = bigMapState.iterator()
        while (iterator.hasNext()) {
            Map.Entry<String, Map<Long, Tuple2<Long, KafkaEventDTO>>> next = iterator.next()
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
    boolean evaluateEventResults(Map<String, Boolean> eventCodeAndWarnResult, String conditionOperator) {
        if (eventCodeAndWarnResult == null || eventCodeAndWarnResult.isEmpty()) {
            return false
        }
        if (eventCodeAndWarnResult.values() == null || eventCodeAndWarnResult.values().isEmpty()) {
            return false
        }
        // 初始化结果变量，根据条件操作符判断初始值
        boolean result = Objects.equals(conditionOperator, RuleCondCombOpEnum.AND.getCode())

        // 遍历事件结果的 Map
        for (Boolean eventResult : eventCodeAndWarnResult.values()) {
            if (conditionOperator == RuleCondCombOpEnum.AND.getCode()) {
                // 对于 AND，只有当所有结果都为 true 时，结果才为 true
                result = eventResult
                // 提前结束循环，如果结果已经为 false
                if (!result) {
                    break
                }
            } else if (conditionOperator == RuleCondCombOpEnum.OR.getCode()) {
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
