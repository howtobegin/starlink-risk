//package com.liboshuai.slr.engine.biz.processor.impl
//
//import com.liboshuai.slr.engine.api.dto.*
//import com.liboshuai.slr.engine.api.enums.RuleCondCombOpEnum
//import com.liboshuai.slr.engine.api.enums.RuleCondTypeEnum
//import com.liboshuai.slr.engine.api.enums.TimeUnitEnum
//import com.liboshuai.slr.engine.api.utils.TimeUtil
//import com.liboshuai.slr.engine.biz.framework.state.ProcessorOneStateDesc
//import com.liboshuai.slr.engine.biz.processor.Processor
//import com.liboshuai.slr.engine.biz.util.CollectionUtil
//import com.liboshuai.slr.engine.biz.util.RedisUtil
//import com.liboshuai.slr.engine.biz.util.RuleEventAttrCompUtil
//import com.liboshuai.slr.framework.common.constants.RedisKeyConstants
//import com.liboshuai.slr.framework.common.enums.CommonStatusEnum
//import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils
//import com.liboshuai.slr.framework.common.util.string.TemplateUtil
//import org.apache.flink.api.common.functions.RuntimeContext
//import org.apache.flink.api.common.state.*
//import org.apache.flink.api.java.tuple.Tuple2
//import org.apache.flink.util.Collector
//import org.apache.flink.util.StringUtils
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
//
//public class ProcessorOne implements Processor {
//
//    private static final Logger log = LoggerFactory.getLogger(ProcessorOne.class);
//
//    /**
//     * 规则信息
//     */
//    private RuleInfoDTO ruleInfoDTO;
//    /**
//     * - key: f0为事件字段，f1为时间戳
//     * - value: f0为eventValue累加值，f1为最新的事件数据
//     */
//    private MapState<Tuple2<String, Long>, Tuple2<Long, FlinkEventDTO>> smallMapState;
//    /**
//     * 记录对于事件条件是否初始化过
//     * - key: eventField
//     * - value: 任意值
//     * （注意不要使用ListState，它查找指定元素的性能很差）
//     */
//    private MapState<String, Boolean> smallInitMapState;
//    /**
//     * 记录是否使用了状态
//     */
//    private ValueState<Boolean> hasValueState;
//    /**
//     * 最新的事件数据
//     */
//    private ValueState<FlinkEventDTO> lastEventState;
//    /**
//     * 规则最近一次触发预警时间
//     */
//    private ValueState<Long> lastWarningTimeState;
//    /**
//     * 最新更新的事件阈值
//     * - key: eventField
//     * - value: 最新更新eventThreshold
//     */
//    private MapState<String, Long> latestEventThresholdMapState;
//    /**
//     * key: f0为eventField，f1为时间戳
//     * value: eventValue累加值
//     */
//    private MapState<Tuple2<String, Long>, Long> bigMapState;
//
//    // 上一个同规则的运算机残留状态（仅用于测试打印日志使用）
//    // private MapState<Tuple2<String, Long>, Tuple2<Long, Long>> oldBigMapState;
//
//    /**
//     * 初始化方法，用于在运行时上下文中注册各种状态
//     *
//     * @param runtimeContext 运行时上下文，用于访问状态和其它运行时设施
//     * @param ruleInfoDTO    规则信息数据传输对象，包含规则特定的元数据
//     */
//    @Override
//    public void init(RuntimeContext runtimeContext, KeyedStateStore keyedStateStore, RuleInfoDTO ruleInfoDTO) {
//        this.ruleInfoDTO = ruleInfoDTO;
//        Long ruleCode = ruleInfoDTO.getRuleCode();
//        Long ruleVersion = ruleInfoDTO.getRuleVersion();
//
//        boolean isRuntimeContextPresent = Objects.nonNull(runtimeContext);
//
//        smallMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getSmallMapStateDesc(ruleCode, ruleVersion));
//        smallInitMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getSmallInitMapStateDesc(ruleCode, ruleVersion));
//        hasValueState = getValueState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getHasValueState(ruleCode, ruleVersion));
//        lastEventState = getValueState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getLastEventStateDesc(ruleCode, ruleVersion));
//        lastWarningTimeState = getValueState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getLastWarningTimeStateDesc(ruleCode, ruleVersion));
//        latestEventThresholdMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getLatestEventThresholdMapStateDesc(ruleCode, ruleVersion));
//        bigMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getGigMapStateDesc(ruleCode, ruleVersion));
//
//        // 上一个同规则的运算机残留状态（仅用于测试打印日志使用）
////         oldBigMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getGigMapStateDesc(ruleCode, ruleVersion - 1));
//    }
//
//    private <K, V> MapState<K, V> getMapState(boolean isRuntimeContextPresent, RuntimeContext runtimeContext, KeyedStateStore keyedStateStore, MapStateDescriptor<K, V> descriptor) {
//        return isRuntimeContextPresent ? runtimeContext.getMapState(descriptor) : keyedStateStore.getMapState(descriptor);
//    }
//
//    private <T> ValueState<T> getValueState(boolean isRuntimeContextPresent, RuntimeContext runtimeContext, KeyedStateStore keyedStateStore, ValueStateDescriptor<T> descriptor) {
//        return isRuntimeContextPresent ? runtimeContext.getState(descriptor) : keyedStateStore.getState(descriptor);
//    }
//
//    /**
//     * 处理元素事件，根据给定的规则信息和Kafka事件进行处理
//     *
//     * @param currentEventTimestamp 时间戳，用于处理的时间参考
//     * @param flinkEventDTO         Kafka事件数据传输对象，包含事件的详细信息
//     * @param out                   用于输出处理结果的收集器
//     * @throws Exception 如果处理过程中遇到任何错误，则抛出异常
//     */
//    @Override
//    public void processElement(String currentKey, long currentEventTimestamp, FlinkEventDTO flinkEventDTO,
//                               Collector<FlinkResultDTO> out) throws Exception {
//        if (Objects.isNull(ruleInfoDTO)) {
//            log.warn("因规则信息为空，故跳过此次计算！当前事件数据：{}", flinkEventDTO);
//            return;
//        }
//        if (!Objects.equals(ruleInfoDTO.getRuleStatus(), CommonStatusEnum.ONLINE.getCode())
//                && !Objects.equals(ruleInfoDTO.getRuleStatus(), CommonStatusEnum.OFFLINE_PENDING.getCode())) {
//            log.warn("因规则[{}]的状态不为'已上线'或'下线待审核'，故跳过此次计算！当前事件数据：{}", ruleInfoDTO.getRuleCode(), flinkEventDTO);
//            return;
//        }
//        // 事件与规则渠道匹配不上，则直接跳过
//        if (!Objects.equals(flinkEventDTO.getChannel(), ruleInfoDTO.getChannel())) {
//            return;
//        }
//        // 事件与规则目标匹配不上，则直接跳过
//        if (!Objects.equals(flinkEventDTO.getTargetField(), ruleInfoDTO.getTargetField())) {
//            return;
//        }
//        // 获取规则条件
//        List<RuleCondDTO> condGroupList = ruleInfoDTO.getRuleCondGroup();
//        if (condGroupList == null || condGroupList.isEmpty()) {
//            log.warn("因规则[{}]的条件组为空，故跳过此次计算！当前事件数据：{}", ruleInfoDTO.getRuleCode(), flinkEventDTO);
//            return;
//        }
//        // 此模型仅支持条件为周期类型的规则
//        for (RuleCondDTO condGroupDTO : condGroupList) {
//            String type = condGroupDTO.getCondType();
//            if (!Objects.equals(type, RuleCondTypeEnum.PERIODIC.getCode())) {
//                log.warn("因规则[{}]的条件类型不为周期类型，故跳过此次计算！当前事件数据：{}", ruleInfoDTO.getRuleCode(), flinkEventDTO);
//                return;
//            }
//        }
//        // 计算规则条件值
//        processRuleCondValue(currentEventTimestamp, ruleInfoDTO, flinkEventDTO, out);
//    }
//
//    /**
//     * 处理规则条件值
//     * 该方法主要用于处理一组规则条件DTO，通过与Kafka事件DTO进行匹配来更新状态值
//     * 如果规则条件跨越历史时间段，则需要从Redis中获取历史事件值，并进行初始化
//     *
//     * @param currentEventTimestamp 时间戳，用于非跨历史时间段的事件匹配
//     * @param flinkEventDTO         Kafka事件DTO
//     */
//    private void processRuleCondValue(long currentEventTimestamp, RuleInfoDTO ruleInfoDTO,
//                                      FlinkEventDTO flinkEventDTO, Collector<FlinkResultDTO> out) throws Exception {
//        List<RuleCondDTO> ruleCondGroup = ruleInfoDTO.getRuleCondGroup();
//        for (RuleCondDTO ruleCondDTO : ruleCondGroup) {
//            // 事件与规则中的事件编号匹配不上，则直接跳过
//            if (!Objects.equals(flinkEventDTO.getEventField(), ruleCondDTO.getEventField())) {
//                // 事件编号匹配不上，则直接跳过
//                continue;
//            }
//            // 进行事件属性匹配
//            List<RuleEventAttrValueDTO> ruleEventAttrValueGroup = ruleCondDTO.getRuleEventAttrValueGroup();
//            boolean eventAttributeMatchResult = matchEventAttribute(ruleEventAttrValueGroup, flinkEventDTO);
//            if (!eventAttributeMatchResult) {
//                // 事件属性匹配不上，则直接跳过
//                continue;
//            }
//            // 规则状态历史的记录数据
//            Boolean hasState = hasValueState.value();
//            if (Objects.isNull(hasState) || !hasState) {
//                StateDTO stateDTO = StateDTO.builder()
//                        .ruleCode(ruleInfoDTO.getRuleCode())
//                        .ruleVersion(ruleInfoDTO.getRuleVersion())
//                        .channel(ruleInfoDTO.getChannel())
//                        .targetField(flinkEventDTO.getTargetField())
//                        .targetValue(flinkEventDTO.getTargetValue())
//                        .build();
//                out.collect(FlinkResultDTO.builder().stateDTO(stateDTO).build());
//                hasValueState.update(true);
//            }
//            // 状态值防空
//            Tuple2<Long, FlinkEventDTO> eventValueAndEventDateTuple2 = smallMapState.get(Tuple2.of(flinkEventDTO.getEventField(), currentEventTimestamp));
//            if (Objects.isNull(eventValueAndEventDateTuple2)) {
//                smallMapState.put(Tuple2.of(flinkEventDTO.getEventField(), currentEventTimestamp), Tuple2.of(0L, flinkEventDTO));
//            }
//            // 规则事件值计算
//            if (ruleCondDTO.getCrossHistory()) { //跨历史时间段
//                String crossHistoryTimeline = ruleCondDTO.getCrossHistoryTimeline();
//                // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，
//                // 所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据
//                if (flinkEventDTO.getEventTime()
//                        <= LocalDateTimeUtils.convertString2Timestamp(crossHistoryTimeline)) {
//                    continue;
//                }
//                // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，
//                // 所以检查当前值是否已经通过redis初始化后，防止重复初始化
//                if (!smallInitMapState.contains(flinkEventDTO.getEventField())) {
//                    // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值
//                    // （注意：Groovy字符串拼接的方式很麻烦，故使用StringBuilder）
//                    String redisKey = buildRedisKey(ruleCondDTO);
//                    String redisHashKey = buildRedisHashKey(flinkEventDTO);
//                    // 注意：因为上面获取历史缓存数据时，使用的是 <= 所以 redis 存储值时查询 doris 要包含历史截至时间点
//                    String initValue = RedisUtil.hget(redisKey, redisHashKey);
//                    RedisUtil.hdel(redisKey, redisHashKey);
//                    if (StringUtils.isNullOrWhitespaceOnly(initValue)) {
//                        log.warn("因规则[{}]的redis初始值为空，故跳过此次计算！redisKey: {}, redisHashKey: {}, 当前事件数据：{}", ruleInfoDTO.getRuleCode(), redisKey, redisHashKey, flinkEventDTO);
//                        return;
//                    }
//                    smallMapState.put(Tuple2.of(flinkEventDTO.getEventField(), currentEventTimestamp), Tuple2.of(Long.parseLong(initValue), flinkEventDTO));
//                    smallInitMapState.put(flinkEventDTO.getEventField(), true);
//                }
//                // 从redis初始化值后，正常处理数据
//                addEventValue(currentEventTimestamp, flinkEventDTO);
//            } else { // 非跨历史时间段
//                // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据
//                if (flinkEventDTO.getEventTime() != currentEventTimestamp) {
//                    continue;
//                }
//                addEventValue(currentEventTimestamp, flinkEventDTO);
//            }
//        }
//    }
//
//    private void addEventValue(long currentEventTimestamp, FlinkEventDTO flinkEventDTO) throws Exception {
//        Tuple2<Long, FlinkEventDTO> eventValueAndEventDateTuple2 = smallMapState.get(Tuple2.of(flinkEventDTO.getEventField(), currentEventTimestamp));
//        Long currentValue = eventValueAndEventDateTuple2.f0;
//        Long newValue = currentValue + Long.parseLong(flinkEventDTO.getEventValue());
//        smallMapState.put(Tuple2.of(flinkEventDTO.getEventField(), currentEventTimestamp), Tuple2.of(newValue, flinkEventDTO));
//    }
//
//
//    /**
//     * 构建Redis的哈希键
//     */
//    private String buildRedisHashKey(FlinkEventDTO flinkEventDTO) {
//        String targetField = flinkEventDTO.getTargetField();
//        String targetValue = flinkEventDTO.getTargetValue();
//        return targetField +
//                RedisKeyConstants.REDIS_KEY_SPLIT +
//                targetValue;
//    }
//
//    /**
//     * 构建Redis的key
//     */
//    private String buildRedisKey(RuleCondDTO ruleCondDTO) {
//        return RedisKeyConstants.DORIS_EVENT_HISTORY_VALUE +
//                RedisKeyConstants.REDIS_KEY_SPLIT +
//                ruleCondDTO.getRuleCode() +
//                RedisKeyConstants.REDIS_KEY_SPLIT +
//                ruleCondDTO.getEventField();
//    }
//
//    /**
//     * 匹配规则事件属性与Kafka事件属性是否符合
//     * 此方法的目的是为了验证给定的Kafka事件是否满足规则事件中定义的所有属性条件
//     * 它通过比较规则事件属性和Kafka事件属性来确定两者是否匹配
//     *
//     * @param flinkEventDTO Kafka事件DTO，包含Kafka事件的详细信息，包括事件属性
//     * @return boolean 如果Kafka事件属性与规则事件属性完全匹配，则返回true；否则返回false
//     */
//    private boolean matchEventAttribute(List<RuleEventAttrValueDTO> ruleEventAttrValueGroup, FlinkEventDTO flinkEventDTO) {
//        if (CollectionUtil.isEmptyOrContainsNulls(ruleEventAttrValueGroup)) {
//            // 规则中不包含事件属性相关的配置，则表明不需要进行事件属性匹配，直接跳过即可
//            return true;
//        }
//        // 逐一便利验证事件属性
//        for (RuleEventAttrValueDTO ruleEventAttrValueDTO : ruleEventAttrValueGroup) {
//            String attrValue = ruleEventAttrValueDTO.getAttrValue();
//            if (StringUtils.isNullOrWhitespaceOnly(attrValue)) {
//                // 规则中不包含事件属性值相关的配置，则表明不需要进行事件属性值匹配，直接跳过即可
//                continue;
//            }
//            String attrField = ruleEventAttrValueDTO.getAttrField();
//            Map<String, String> kafkaEventAttrMap = flinkEventDTO.getEventAttrMap();
//            if (Objects.isNull(kafkaEventAttrMap) || kafkaEventAttrMap.isEmpty()) {
//                // 规则包含事件属性配置，但是kafka数据事件属性map为空，故直接判定为不符合规则要求
//                log.warn("规则包含事件属性配置，但是kafka数据事件属性map为空，故直接判定为不符合规则要求！" +
//                        "规则事件属性信息:{}, 当前事件信息:{}", ruleEventAttrValueDTO, flinkEventDTO);
//                return false;
//            }
//            if (!kafkaEventAttrMap.containsKey(attrField)) {
//                // kafka事件属性不包含规则中事件属性，则表明不符合匹配
//                log.warn("kafka数据事件属性map并不包含规则配置的事件属性Field，故直接判定为不符合规则要求！" +
//                        "规则事件属性信息:{}, 当前事件信息:{}", ruleEventAttrValueDTO, flinkEventDTO);
//                return false;
//            }
//            String kafkaEventAttributeValue = kafkaEventAttrMap.get(attrField);
//            if (Objects.isNull(kafkaEventAttributeValue)) {
//                // kafka事件中对于规则中事件属性值为空，则表明不符合匹配
//                return false;
//            }
//            // 比较kafka中属性值与规则中属性值
//            boolean isMatch = RuleEventAttrCompUtil.compareValues(ruleEventAttrValueDTO, flinkEventDTO);
//            if (!isMatch) {
//                // kafka事件属性值与规则事件属性值不相等，则表明不符合匹配
//                return false;
//            }
//        }
//        // 所有事件属性都匹配，则表明符合匹配
//        return true;
//    }
//
//    /**
//     * 定时器触发时执行的方法
//     *
//     * @param timestamp 时间戳，表示当前时间
//     * @param out       输出收集器，用于收集和输出预警信息
//     * @throws Exception 可能抛出的异常
//     */
//    @Override
//    public boolean onTimer(String currentKey, long timestamp, Collector<FlinkResultDTO> out) throws Exception {
//        if (Objects.isNull(ruleInfoDTO)) {
//            log.warn("因规则信息为空，故跳过此次计算！");
//            return true;
//        }
//        // 获取规则条件
//        List<RuleCondDTO> ruleCondGroup = ruleInfoDTO.getRuleCondGroup();
//        if (ruleCondGroup == null || ruleCondGroup.isEmpty()) {
//            log.warn("因规则[{}]的条件组为空，故跳过此次计算！", ruleInfoDTO.getRuleCode());
//            return false;
//        }
//        // 将规则条件根据事件编号存储到map中，方便后续操作
//        Map<String, RuleCondDTO> ruleConditionMapByEventField = new HashMap<>();
//        for (RuleCondDTO ruleCondDTO : ruleCondGroup) {
//            ruleConditionMapByEventField.put(ruleCondDTO.getEventField(), ruleCondDTO);
//        }
//        // 将小时间窗口（步长窗口）中的数据累加到大时间窗口（整体窗口）中，并返回最新（时间戳最大）的事件数据。
//        aggregateSmallMapToBigMap(timestamp);
//        // 清理窗口大小之外的数据
//        cleanupWindowData(timestamp, ruleConditionMapByEventField);
//        // 处理bigMapState
//        Tuple2<Boolean, ProcessorDTO> processBigMapResult = processBigMap(ruleConditionMapByEventField, ruleInfoDTO.getRuleCondCombOp());
//        // 根据规则中事件条件表达式组合判断事件结果 与预警频率 判断否是触发预警
//        if (lastWarningTimeState.value() == null) {
//            lastWarningTimeState.update(0L);
//        }
//        // 获取预警间隔时间，单位为毫秒
//        Long alertInterval = getAlertInterval(ruleInfoDTO);
//        // 检查是否需要发送预警
//        boolean shouldAlert = processBigMapResult.f0 &&
//                (alertInterval == null || (timestamp - lastWarningTimeState.value() >= alertInterval));
//        if (shouldAlert) {
//            // 更新最后预警时间
//            lastWarningTimeState.update(timestamp);
//            // 发送预警信息
//            AlertDTO alertDTO = buildAlert(ruleInfoDTO, lastEventState.value(), processBigMapResult);
//            log.warn("最终推送的预警信息内容：{}, 当前Key: {}", alertDTO, currentKey);
//            FlinkResultDTO flinkResultDTO = FlinkResultDTO.builder().alertDTO(alertDTO).build();
//            out.collect(flinkResultDTO);
//        }
//        return hasActiveEvents();
//    }
//
//    private void logState(Long ruleCode, String currentKey) throws Exception {
//        Map<Tuple2<String, Long>, Tuple2<Long, FlinkEventDTO>> smallMap = new HashMap<>();
//        for (Map.Entry<Tuple2<String, Long>, Tuple2<Long, FlinkEventDTO>> entry : smallMapState.entries()) {
//            smallMap.put(entry.getKey(), entry.getValue());
//        }
//        Map<Tuple2<String, Long>, Long> bigMap = new HashMap<>();
//        for (Map.Entry<Tuple2<String, Long>, Long> entry : bigMapState.entries()) {
//            bigMap.put(entry.getKey(), entry.getValue());
//        }
//        log.warn("onTime计算触发，ruleCode:{}, currentKey：{}, smallMap:{}, bigMap：{}", ruleCode, currentKey, smallMap, bigMap);
//    }
//
////    private void logOldState(Long ruleCode, String currentKey) throws Exception {
////        Map<Tuple2<String, Long>, Tuple2<Long, Long>> bigMap = new HashMap<>();
////        for (Map.Entry<Tuple2<String, Long>, Tuple2<Long, Long>> entry : oldBigMapState.entries()) {
////            bigMap.put(entry.getKey(), entry.getValue());
////        }
////        log.warn("残留旧状态，ruleCode:{}, currentKey：{}, bigMap：{}", ruleCode, currentKey, bigMap);
////    }
//
//    /**
//     * 构建预警信息的方法，提取重复逻辑
//     */
//    private AlertDTO buildAlert(RuleInfoDTO ruleInfoDTO, FlinkEventDTO latestFlinkEventDTO, Tuple2<Boolean, ProcessorDTO> processBigMapResult) {
//        String finalWarnMessage = TemplateUtil.replacePlaceholders(
//                ruleInfoDTO.getAlertTemplate(),
//                ruleInfoDTO,
//                latestFlinkEventDTO,
//                processBigMapResult.f1
//        );
//        return AlertDTO.builder()
//                .channel(ruleInfoDTO.getChannel())
//                .ruleCode(ruleInfoDTO.getRuleCode())
//                .message(finalWarnMessage)
//                .time(LocalDateTimeUtils.convertTimestamp2String(System.currentTimeMillis()))
//                .targetField(ruleInfoDTO.getTargetField())
//                .targetValue(latestFlinkEventDTO.getTargetValue())
//                .eventValueGroup(processBigMapResult.f1.getEventValueGroup())
//                .build();
//    }
//
//    private Long getAlertInterval(RuleInfoDTO ruleInfoDTO) {
//        Long alertIntervalValue = ruleInfoDTO.getAlertIntervalValue();
//        String alertIntervalUnit = ruleInfoDTO.getAlertIntervalUnit();
//        if (Objects.isNull(alertIntervalValue) || Objects.isNull(alertIntervalUnit)) {
//            return null;
//        }
//        return TimeUtil.toMillis(alertIntervalValue, TimeUnitEnum.fromEnUnit(alertIntervalUnit));
//    }
//
//    /**
//     * 检查是否存在活跃的事件
//     * 该方法用于遍历一个大的状态映射，以确定其中是否包含活跃的Kafka事件
//     *
//     * @return boolean - 如果存在活跃的事件，则返回true；否则返回false
//     */
//    private boolean hasActiveEvents() throws Exception {
//        return bigMapState.entries().iterator().hasNext();
//    }
//
//    /**
//     * 处理大映射表中的数据以确定是否满足规则条件
//     * 此方法主要负责遍历大映射表状态，计算每个事件字段的累加值，判断是否满足规则条件，并返回相关的处理结果
//     *
//     * @param ruleConditionMapByEventField 按事件字段分类的规则条件映射表
//     * @param ruleCondCombOp               规则条件的组合操作符，用于确定如何组合多个规则条件的结果
//     * @return 返回一个Tuple2对象，包含事件结果、处理器DTO
//     * @throws Exception 如果处理过程中发生错误，则抛出异常
//     */
//    private Tuple2<Boolean, ProcessorDTO> processBigMap(Map<String, RuleCondDTO> ruleConditionMapByEventField,
//                                                        String ruleCondCombOp) throws Exception {
//        // 获取事件与之判断结果
//        Map<String, Boolean> eventFieldAndWarnResult = new HashMap<>();
//        // 获取事件字段与值之和
//        Map<String, Long> eventFiledAndValueSumMap = new HashMap<>();
//        // 遍历 MapState 的所有条目
//        for (Map.Entry<Tuple2<String, Long>, Long> entry : bigMapState.entries()) {
//            Tuple2<String, Long> key = entry.getKey(); // 获取键，包含 eventField 和关联的时间戳值
//            Long eventValue = entry.getValue(); // 获取事件累加值
//            String eventField = key.f0; // Tuple2 的第一个元素作为事件字段
//            // 使用 merge 方法高效地累加值
//            eventFiledAndValueSumMap.merge(eventField, eventValue, Long::sum);
//        }
//        // 确保所有规则条件中的事件字段都被包含，如果不存在则设置为 0L
//        Set<String> eventFieldSet = ruleConditionMapByEventField.keySet();
//        for (String eventField : eventFieldSet) {
//            eventFiledAndValueSumMap.putIfAbsent(eventField, 0L);
//        }
//        // 判断是否触发规则事件阈值
//        for (String eventField : eventFieldSet) {
//            Long eventValueSum = eventFiledAndValueSumMap.get(eventField);
//            RuleCondDTO ruleCondDTO = ruleConditionMapByEventField.get(eventField);
//            // 获取事件的阈值
//            Long eventThreshold = getEventThreshold(eventField, ruleCondDTO);
//            eventFieldAndWarnResult.put(eventField, eventValueSum > eventThreshold);
//        }
//        boolean eventResult = evaluateEventResults(eventFieldAndWarnResult, ruleCondCombOp);
//        // 构建运算机的DTO对象
//        ProcessorDTO processorDTO = ProcessorDTO.builder()
//                .eventValueGroup(eventFiledAndValueSumMap)
//                .build();
//        return Tuple2.of(eventResult, processorDTO);
//    }
//
//    /**
//     * 获取事件的阈值
//     * 此方法用于计算和获取给定规则条件下的事件阈值如果设置了阈值缩放因子，则使用之；
//     * 否则直接返回规则条件中的阈值
//     *
//     * @param eventField  事件字段，用于查找可能已经存在的阈值
//     * @param ruleCondDTO 规则条件对象，包含阈值和阈值缩放因子
//     * @return 计算后的事件阈值
//     * @throws Exception 如果计算过程中发生错误，则抛出异常
//     */
//    private Long getEventThreshold(String eventField, RuleCondDTO ruleCondDTO) throws Exception {
//        Long eventThreshold = ruleCondDTO.getThreshold();
//        Long thresholdScaleFactor = ruleCondDTO.getThresholdScaleFactor();
//        if (Objects.nonNull(thresholdScaleFactor)) {
//            Long latestThreshold = latestEventThresholdMapState.get(eventField);
//            // FIXME: 逻辑错误，应该对应条件预警触发后，才更新
//            if (Objects.nonNull(latestThreshold)) {
//                eventThreshold = latestThreshold * thresholdScaleFactor;
//            }
//            latestEventThresholdMapState.put(eventField, eventThreshold);
//        }
//        return eventThreshold;
//    }
//
//
//    /**
//     * 将小时间窗口（步长窗口）中的数据累加到大时间窗口（整体窗口）中，并更新最新（时间戳最大）的事件数据。
//     */
//    private void aggregateSmallMapToBigMap(long timestamp) throws Exception {
//        // 遍历 smallMapState 的所有条目
//        Long timestampMax = 0L;
//        for (Map.Entry<Tuple2<String, Long>, Tuple2<Long, FlinkEventDTO>> smallMapEntry : smallMapState.entries()) {
//            Tuple2<String, Long> eventFieldAndTimeTuple2 = smallMapEntry.getKey();
//            Tuple2<Long, FlinkEventDTO> eventValueAndEventDataTuple2 = smallMapEntry.getValue();
//            // 创建新的 Tuple2 作为 bigMapState 的键
//            Tuple2<String, Long> tupleKey = Tuple2.of(eventFieldAndTimeTuple2.f0, timestamp);
//            // 将 (eventField, timestamp) 作为键，eventValue 作为值，存入 bigMapState
//            bigMapState.put(tupleKey, eventValueAndEventDataTuple2.f0);
//            // 更新最新的事件数据
//            Long eventTime = eventFieldAndTimeTuple2.f1;
//            if (eventTime > timestampMax) {
//                timestampMax = eventTime;
//                lastEventState.update(eventValueAndEventDataTuple2.f1);
//            }
//        }
////        logState(xxx, xxx);
//        // 当前窗口步长的数据已经添加到窗口中了，清空当前key状态
//        smallMapState.clear();
//    }
//
//    /**
//     * 清理窗口大小之外的数据
//     */
//    private void cleanupWindowData(long timestamp, Map<String, RuleCondDTO> ruleConditionMapByEventField) throws Exception {
//        // 提前计算每个 eventField 的 windowSize 和 windowThresholdTime
//        Map<String, Long> eventFieldToThresholdTime = new HashMap<>();
//
//        for (Map.Entry<String, RuleCondDTO> entry : ruleConditionMapByEventField.entrySet()) {
//            String eventField = entry.getKey();
//            RuleCondDTO ruleCondDTO = entry.getValue();
//            long windowSize = TimeUtil.toMillis(ruleCondDTO.getWindowValue(),
//                    TimeUnitEnum.fromEnUnit(ruleCondDTO.getWindowUnit()));
//            long windowThresholdTime = timestamp - windowSize;
//            eventFieldToThresholdTime.put(eventField, windowThresholdTime);
//        }
//
//        // 遍历 bigMapState 的所有条目
//        Iterator<Map.Entry<Tuple2<String, Long>, Long>> iterator = bigMapState.entries().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<Tuple2<String, Long>, Long> stateEntry = iterator.next();
//            Tuple2<String, Long> eventFieldAndTimeTuple2 = stateEntry.getKey();
//            String eventField = eventFieldAndTimeTuple2.f0;
//            Long eventTime = eventFieldAndTimeTuple2.f1;
//
//            RuleCondDTO ruleCondDTO = ruleConditionMapByEventField.get(eventField);
//            if (Objects.isNull(ruleCondDTO)) {
//                log.warn("清理窗口大小之外的数据时，存在规则条件中不存在的 eventField: {}", eventField);
//                continue;
//            }
//
//            Long windowThresholdTime = eventFieldToThresholdTime.get(eventField);
//            if (eventTime <= windowThresholdTime) {
//                // 删除过期的条目
//                iterator.remove();
//            }
//        }
//    }
//
//    /**
//     * 评估事件结果，根据给定的条件操作符返回最终结果。
//     *
//     * @param eventFieldAndWarnResult 包含事件代码及其对应的警告结果的映射
//     * @param conditionOperator       条件操作符，支持 AND 和 OR
//     * @return 根据条件操作符计算后的最终结果（true 或 false）
//     */
//    private boolean evaluateEventResults(Map<String, Boolean> eventFieldAndWarnResult, String conditionOperator) {
//        // 检查输入是否为 null 或为空
//        if (eventFieldAndWarnResult == null || eventFieldAndWarnResult.isEmpty()) {
//            return false;
//        }
//
//        // 如果只有一个元素，直接返回该元素的值
//        if (eventFieldAndWarnResult.size() == 1) {
//            return eventFieldAndWarnResult.values().iterator().next();
//        }
//
//        // 确定操作符类型
//        RuleCondCombOpEnum opEnum = RuleCondCombOpEnum.fromCode(conditionOperator);
//
//        // 初始化结果，根据操作符类型
//        boolean result;
//        if (opEnum == RuleCondCombOpEnum.AND) {
//            result = true; // 对于 AND，初始值为 true
//            for (Boolean eventResult : eventFieldAndWarnResult.values()) {
//                if (!eventResult) {
//                    return false; // 任何一个 false 都返回 false
//                }
//            }
//        } else if (opEnum == RuleCondCombOpEnum.OR) {
//            result = false; // 对于 OR，初始值为 false
//            for (Boolean eventResult : eventFieldAndWarnResult.values()) {
//                if (eventResult) {
//                    return true; // 任何一个 true 都返回 true
//                }
//            }
//        } else {
//            log.warn("因规则[{}]的条件组合操作符非法，故跳过此次计算！", ruleInfoDTO.getRuleCode());
//            return false;
//        }
//
//        return result;
//    }
//}
