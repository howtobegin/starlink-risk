package com.liboshuai.slr.engine.biz.processor.impl;

import com.liboshuai.slr.engine.api.dto.*;
import com.liboshuai.slr.engine.api.enums.RuleCondCombOpEnum;
import com.liboshuai.slr.engine.api.enums.RuleCondTypeEnum;
import com.liboshuai.slr.engine.api.enums.TimeUnitEnum;
import com.liboshuai.slr.engine.api.utils.TemplateUtil;
import com.liboshuai.slr.engine.api.utils.TimeRangeUtil;
import com.liboshuai.slr.engine.api.utils.TimeUtil;
import com.liboshuai.slr.engine.biz.framework.state.ProcessorOneStateDesc;
import com.liboshuai.slr.engine.biz.processor.Processor;
import com.liboshuai.slr.engine.biz.util.CollectionUtil;
import com.liboshuai.slr.engine.biz.util.RedisUtil;
import com.liboshuai.slr.engine.biz.util.RuleEventAttrCompUtil;
import com.liboshuai.slr.framework.common.constants.RedisKeyConstants;
import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


public class ProcessorOne implements Processor {

    private static final Logger log = LoggerFactory.getLogger(ProcessorOne.class);

    /**
     * 规则信息
     */
    private RuleInfoDTO ruleInfoDTO;
    /**
     * - key: 事件字段
     * - value: f0为eventValue累加值，f1为最新的事件数据，f2为最新事件数据的时间戳
     */
    private MapState<String, Tuple3<Long, FlinkEventDTO, Long>> smallMapState;
    /**
     * 记录对于事件条件是否初始化过
     * - key: eventField
     * - value: 任意值
     * （注意不要使用ListState，它查找指定元素的性能很差）
     */
    private MapState<String, Boolean> smallInitMapState;
    /**
     * 记录是否使用了状态
     */
    private ValueState<Boolean> hasValueState;
    /**
     * 是否在时间范围内
     */
    private MapState<String, Boolean> inTimeRangeMapState;
    /**
     * 下一个时间范围的结束时间戳
     */
    private MapState<String, Long> nextEndTimestampState;
    /**
     * 最新的事件数据
     */
    private ValueState<FlinkEventDTO> lastEventState;
    /**
     * 规则最近一次触发预警时间
     */
    private ValueState<Long> lastAlertTimeState;
    /**
     * 最新更新的事件阈值
     * - key: eventField
     * - value: 最新更新eventThreshold
     */
    private MapState<String, Long> latestEventThresholdMapState;
    /**
     * key: f0为eventField，f1为时间戳
     * value: eventValue累加值
     */
    private MapState<Tuple2<String, Long>, Long> bigMapState;

    // 上一个同规则的运算机残留状态（仅用于测试打印日志使用）
    // private MapState<Tuple2<String, Long>, Tuple2<Long, Long>> oldBigMapState;

    /**
     * 初始化方法，用于在运行时上下文中注册各种状态
     *
     * @param runtimeContext 运行时上下文，用于访问状态和其它运行时设施
     * @param ruleInfoDTO    规则信息数据传输对象，包含规则特定的元数据
     */
    @Override
    public void init(RuntimeContext runtimeContext, KeyedStateStore keyedStateStore, RuleInfoDTO ruleInfoDTO) {
        this.ruleInfoDTO = ruleInfoDTO;
        Long ruleCode = ruleInfoDTO.getRuleCode();
        Long ruleVersion = ruleInfoDTO.getRuleVersion();

        boolean isRuntimeContextPresent = Objects.nonNull(runtimeContext);

        smallMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getSmallMapStateDesc(ruleCode, ruleVersion));
        smallInitMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getSmallInitMapStateDesc(ruleCode, ruleVersion));
        hasValueState = getValueState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getHasValueStateDesc(ruleCode, ruleVersion));
        inTimeRangeMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getInTimeRangeStateDesc(ruleCode, ruleVersion));
        nextEndTimestampState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getNextEndTimestampStateDesc(ruleCode, ruleVersion));
        lastEventState = getValueState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getLastEventStateDesc(ruleCode, ruleVersion));
        lastAlertTimeState = getValueState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getLastAlertTimeStateDesc(ruleCode, ruleVersion));
        latestEventThresholdMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getLatestEventThresholdMapStateDesc(ruleCode, ruleVersion));
        bigMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getGigMapStateDesc(ruleCode, ruleVersion));

        // 上一个同规则的运算机残留状态（仅用于测试打印日志使用）
//        oldBigMapState = getMapState(isRuntimeContextPresent, runtimeContext, keyedStateStore, ProcessorOneStateDesc.getGigMapStateDesc(ruleCode, ruleVersion - 1));
    }

    private <K, V> MapState<K, V> getMapState(boolean isRuntimeContextPresent, RuntimeContext runtimeContext, KeyedStateStore keyedStateStore, MapStateDescriptor<K, V> descriptor) {
        return isRuntimeContextPresent ? runtimeContext.getMapState(descriptor) : keyedStateStore.getMapState(descriptor);
    }

    private <T> ValueState<T> getValueState(boolean isRuntimeContextPresent, RuntimeContext runtimeContext, KeyedStateStore keyedStateStore, ValueStateDescriptor<T> descriptor) {
        return isRuntimeContextPresent ? runtimeContext.getState(descriptor) : keyedStateStore.getState(descriptor);
    }

    /**
     * 处理元素事件，根据给定的规则信息和Kafka事件进行处理
     *
     * @param flinkEventDTO Kafka事件数据传输对象，包含事件的详细信息
     * @param out           用于输出处理结果的收集器
     * @throws Exception 如果处理过程中遇到任何错误，则抛出异常
     */
    @Override
    public boolean processElement(KeyedBroadcastProcessFunction.ReadOnlyContext ctx, long timestamp,
                               FlinkEventDTO flinkEventDTO, Collector<FlinkResultDTO> out) throws Exception {
        // 前置效验处理
        List<RuleCondDTO> condGroupList = afterCheckHandler(flinkEventDTO);
        if (condGroupList == null) {
            return false;
        }
        // 将规则条件根据事件编号存储到map中，方便后续操作
        Map<String, RuleCondDTO> ruleConditionMapByEventField = new HashMap<>();
        for (RuleCondDTO ruleCondDTO : condGroupList) {
            ruleConditionMapByEventField.put(ruleCondDTO.getEventField(), ruleCondDTO);
        }
        // 获取并效验条件类型
        String condType = getCondType(ruleConditionMapByEventField);
        // 根据条件类型进行不同处理
        if (Objects.equals(condType, RuleCondTypeEnum.RECENT.getCode())) { // 最近时间类型
            return processElementRecent(timestamp, flinkEventDTO, out);
        } else if (Objects.equals(condType, RuleCondTypeEnum.RANGE.getCode())) { // 范围时间类型
            processElementRange(ctx, timestamp, flinkEventDTO, out, condType, ruleConditionMapByEventField);
            return false;
        } else {
            log.warn("因规则[{}]中事件条件类型为未知值[{}]，故跳过此次计算！当前事件数据：{}", ruleInfoDTO.getRuleCode(), condType, flinkEventDTO);
            return false;
        }
    }

    /**
     * 前置效验处理
     */
    private List<RuleCondDTO> afterCheckHandler(FlinkEventDTO flinkEventDTO) {
        if (Objects.isNull(ruleInfoDTO)) {
            log.warn("因规则信息为空，故跳过此次计算！当前事件数据：{}", flinkEventDTO);
            return null;
        }
        if (!Objects.equals(ruleInfoDTO.getRuleStatus(), CommonStatusEnum.ONLINE.getCode())
                && !Objects.equals(ruleInfoDTO.getRuleStatus(), CommonStatusEnum.OFFLINE_PENDING.getCode())) {
            log.warn("因规则[{}]的状态不为'已上线'或'下线待审核'，故跳过此次计算！当前事件数据：{}", ruleInfoDTO.getRuleCode(), flinkEventDTO);
            return null;
        }
        // 事件与规则渠道匹配不上，则直接跳过
        if (!Objects.equals(flinkEventDTO.getChannel(), ruleInfoDTO.getChannel())) {
            return null;
        }
        // 事件与规则目标匹配不上，则直接跳过
        if (!Objects.equals(flinkEventDTO.getTargetField(), ruleInfoDTO.getTargetField())) {
            return null;
        }
        // 获取规则条件
        List<RuleCondDTO> condGroupList = ruleInfoDTO.getRuleCondGroup();
        if (condGroupList == null || condGroupList.isEmpty()) {
            log.warn("因规则[{}]的条件组为空，故跳过此次计算！当前事件数据：{}", ruleInfoDTO.getRuleCode(), flinkEventDTO);
            return null;
        }
        return condGroupList;
    }

    /**
     * 计算处理最近范围时间类型的事件规则数据
     */
    private void processElementRange(KeyedBroadcastProcessFunction.ReadOnlyContext ctx, long timestamp,
                                     FlinkEventDTO flinkEventDTO, Collector<FlinkResultDTO> out,
                                     String condType, Map<String, RuleCondDTO> ruleConditionMapByEventField) throws Exception {
        // 处理时间范围
        handleTimeRange(ctx, timestamp, flinkEventDTO, out);
        // 处理smallMapState
        Tuple2<Boolean, ProcessorDTO> processSmallMapResult = processSmallMap(ruleConditionMapByEventField);
        // 处理预警结果
        handleAlertResult(ctx, timestamp, out, condType, processSmallMapResult);
    }

    /**
     * 处理预警结果
     */
    private void handleAlertResult(KeyedBroadcastProcessFunction.ReadOnlyContext ctx, long timestamp,
                                   Collector<FlinkResultDTO> out, String condType,
                                   Tuple2<Boolean, ProcessorDTO> processSmallMapResult) throws IOException {
        // 根据规则中事件条件表达式组合判断事件结果 与预警频率 判断否是触发预警
        if (lastAlertTimeState.value() == null) {
            lastAlertTimeState.update(0L);
        }
        // 获取预警间隔时间，单位为毫秒
        Long alertInterval = getAlertInterval(ruleInfoDTO);
        // 检查是否需要发送预警
        boolean shouldAlert;
        if (Objects.equals(condType, RuleCondTypeEnum.RECENT.getCode())) {
            shouldAlert = processSmallMapResult.f0 &&
                    (alertInterval == null || (timestamp - lastAlertTimeState.value() >= alertInterval));
        } else if (Objects.equals(condType, RuleCondTypeEnum.RANGE.getCode())) {
            shouldAlert = processSmallMapResult.f0;
        } else {
            log.warn("因规则[{}]的条件类型为未知值[{}]，故跳过此次计算！", ruleInfoDTO.getRuleCode(), condType);
            return;
        }
        if (shouldAlert) {
            // 更新最后预警时间
            lastAlertTimeState.update(timestamp);
            // 发送预警信息
            AlertDTO alertDTO = buildAlert(timestamp, ruleInfoDTO, lastEventState.value(), processSmallMapResult.f1);
            log.info("最终推送的预警信息内容：{}, 当前Key: {}", JsonUtils.toJsonString(alertDTO), ctx.getCurrentKey());
            FlinkResultDTO flinkResultDTO = FlinkResultDTO.builder().alertDTO(alertDTO).build();
            out.collect(flinkResultDTO);
        }
    }

    /**
     * 处理时间范围
     */
    private void handleTimeRange(KeyedBroadcastProcessFunction.ReadOnlyContext ctx, long timestamp,
                                 FlinkEventDTO flinkEventDTO, Collector<FlinkResultDTO> out) throws Exception {
        List<RuleCondDTO> ruleCondGroup = ruleInfoDTO.getRuleCondGroup();
        for (RuleCondDTO ruleCondDTO : ruleCondGroup) {
            // 事件与规则中的事件编号匹配不上，则直接跳过
            if (!Objects.equals(flinkEventDTO.getEventField(), ruleCondDTO.getEventField())) {
                // 事件编号匹配不上，则直接跳过
                continue;
            }
            // 进行事件属性匹配
            List<RuleEventAttrValueDTO> ruleEventAttrValueGroup = ruleCondDTO.getRuleEventAttrValueGroup();
            boolean eventAttributeMatchResult = matchEventAttribute(ruleEventAttrValueGroup, flinkEventDTO);
            if (!eventAttributeMatchResult) {
                // 事件属性匹配不上，则直接跳过
                continue;
            }

            // ******************************* 规则匹配成功，进行后续处理 *******************************

            TimeRangeDTO timeRangeDTO = ruleCondDTO.getTimeRange();
            if (Objects.isNull(timeRangeDTO)) {
                log.warn("因规则[{}]中事件条件类型为范围时间类型，而规则条件中时间范围信息为空，故跳过此次计算！当前事件数据：{}", ruleInfoDTO.getRuleCode(), flinkEventDTO);
                continue;
            }
            boolean isWithInTimeRange = TimeRangeUtil.isWithinRule(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()), timeRangeDTO);
            Boolean inTimeRange = inTimeRangeMapState.get(flinkEventDTO.getTargetField());
            if (Objects.isNull(inTimeRange)) {
                inTimeRange = false;
                inTimeRangeMapState.put(flinkEventDTO.getTargetField(), inTimeRange);
            }
            if (!inTimeRange && isWithInTimeRange) {
                inTimeRange = true;
                inTimeRangeMapState.put(flinkEventDTO.getTargetField(), inTimeRange);
                // 注册定时器为时间范围结束时刻
                Long nextEndTimestamp = TimeRangeUtil.getNextEndTimestamp(ctx.currentProcessingTime(), timeRangeDTO);
                ctx.timerService().registerProcessingTimeTimer(nextEndTimestamp);
                // 更新下次结束时刻
                nextEndTimestampState.put(ruleCondDTO.getEventField(), nextEndTimestamp);
            }
            if (!inTimeRange) {
                continue;
            }

            // 规则状态历史的记录数据
            Boolean hasState = hasValueState.value();
            if (Objects.isNull(hasState) || !hasState) {
                StateDTO stateDTO = StateDTO.builder()
                        .ruleCode(ruleInfoDTO.getRuleCode())
                        .ruleVersion(ruleInfoDTO.getRuleVersion())
                        .channel(ruleInfoDTO.getChannel())
                        .targetField(flinkEventDTO.getTargetField())
                        .targetValue(flinkEventDTO.getTargetValue())
                        .build();
                out.collect(FlinkResultDTO.builder().stateDTO(stateDTO).build());
                hasValueState.update(true);
            }
            // 状态值防空
            Tuple3<Long, FlinkEventDTO, Long> tuple3 = smallMapState.get(flinkEventDTO.getEventField());
            if (Objects.isNull(tuple3)) {
                smallMapState.put(flinkEventDTO.getEventField(), Tuple3.of(0L, flinkEventDTO, timestamp));
            }
            // 规则事件值计算
            if (ruleCondDTO.getCrossHistory()) { //跨历史时间段
                String crossHistoryTimeline = ruleCondDTO.getCrossHistoryTimeline();
                // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，
                // 所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据
                if (flinkEventDTO.getEventTime()
                        <= LocalDateTimeUtils.convertString2Timestamp(crossHistoryTimeline)) {
                    continue;
                }
                // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，
                // 所以检查当前值是否已经通过redis初始化后，防止重复初始化
                if (!smallInitMapState.contains(flinkEventDTO.getEventField())) {
                    // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值
                    // （注意：Groovy字符串拼接的方式很麻烦，故使用StringBuilder）
                    String redisKey = buildRedisKey(ruleCondDTO);
                    String redisHashKey = buildRedisHashKey(flinkEventDTO);
                    // 注意：因为上面获取历史缓存数据时，使用的是 <= 所以 redis 存储值时查询 doris 要包含历史截至时间点
                    String initValue = RedisUtil.hget(redisKey, redisHashKey);
                    RedisUtil.hdel(redisKey, redisHashKey);
                    if (StringUtils.isNullOrWhitespaceOnly(initValue)) {
                        log.warn("因规则[{}]的redis初始值为空，故跳过此次计算！redisKey: {}, redisHashKey: {}, 当前事件数据：{}", ruleInfoDTO.getRuleCode(), redisKey, redisHashKey, flinkEventDTO);
                        continue;
                    }
                    smallMapState.put(flinkEventDTO.getEventField(), Tuple3.of(Long.parseLong(initValue), flinkEventDTO, timestamp));
                    smallInitMapState.put(flinkEventDTO.getEventField(), true);
                }
                // 从redis初始化值后，正常处理数据
                addEventValue(timestamp, flinkEventDTO);
            } else { // 非跨历史时间段
                // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据
                if (flinkEventDTO.getEventTime() != timestamp) {
                    continue;
                }
                addEventValue(timestamp, flinkEventDTO);
            }
        }
    }

    /**
     * 处理smallMapState
     */
    private Tuple2<Boolean, ProcessorDTO> processSmallMap(Map<String, RuleCondDTO> ruleConditionMapByEventField) throws Exception {
        Long timestampMax = 0L;
        Map<String, Boolean> eventFieldAndAlertResult = new HashMap<>();
        Map<String, Long> eventFiledAndValueSumMap = new HashMap<>();
        for (Map.Entry<String, Tuple3<Long, FlinkEventDTO, Long>> entry : smallMapState.entries()) {
            String eventField = entry.getKey();
            Tuple3<Long, FlinkEventDTO, Long> tuple3 = entry.getValue();
            Long eventValueSum = tuple3.f0;
            if (Objects.isNull(eventValueSum)) {
                eventValueSum = 0L;
            }
            RuleCondDTO ruleCondDTO = ruleConditionMapByEventField.get(eventField);
            // 获取事件字段与其对应的事件累加值
            eventFiledAndValueSumMap.put(ruleCondDTO.getEventField(), eventValueSum);
            // 获取事件字段与其对应的预警结果
            boolean alertResult = calcAlertResult(ruleCondDTO.getEventField(), eventValueSum,
                    ruleCondDTO.getThreshold(), ruleCondDTO.getThresholdScaleFactor());
            eventFieldAndAlertResult.put(ruleCondDTO.getEventField(), alertResult);
            // 更新最新的事件数据
            Long eventTime = tuple3.f2;
            if (eventTime > timestampMax) {
                timestampMax = eventTime;
                lastEventState.update(tuple3.f1);
            }
        }
        boolean eventResult = evaluateEventResults(eventFieldAndAlertResult, ruleInfoDTO.getRuleCondCombOp());
        // 构建 processorDTO
        ProcessorDTO processorDTO = ProcessorDTO.builder().eventValueGroup(eventFiledAndValueSumMap).build();
        // 构建 processSmallMapResult
        return Tuple2.of(eventResult, processorDTO);
    }

    /**
     * 计算处理最近条件时间类型的事件规则数据
     */
    private boolean processElementRecent(long timestamp, FlinkEventDTO flinkEventDTO, Collector<FlinkResultDTO> out) throws Exception {
        boolean result = false;
        List<RuleCondDTO> ruleCondGroup = ruleInfoDTO.getRuleCondGroup();
        for (RuleCondDTO ruleCondDTO : ruleCondGroup) {
            // 处理单个规则条件的匹配和规则计算逻辑
            boolean tempResult = processRuleCondition(timestamp, flinkEventDTO, out, ruleCondDTO);
            if (tempResult) {
                result = true;
            }
        }
        return result;
    }

    private boolean processRuleCondition2(long timestamp, FlinkEventDTO flinkEventDTO, Collector<FlinkResultDTO> out, RuleCondDTO ruleCondDTO) throws Exception {
        // 事件与规则中的事件编号匹配不上，则直接跳过
        if (!Objects.equals(flinkEventDTO.getEventField(), ruleCondDTO.getEventField())) {
            // 事件编号匹配不上，则直接跳过
            return false;
        }
        // 进行事件属性匹配
        List<RuleEventAttrValueDTO> ruleEventAttrValueGroup = ruleCondDTO.getRuleEventAttrValueGroup();
        boolean eventAttributeMatchResult = matchEventAttribute(ruleEventAttrValueGroup, flinkEventDTO);
        if (!eventAttributeMatchResult) {
            // 事件属性匹配不上，则直接跳过
            return false;
        }

        // ******************************* 规则匹配成功，进行后续处理 *******************************

        // 规则状态历史的记录数据
        Boolean hasState = hasValueState.value();
        if (Objects.isNull(hasState) || !hasState) {
            StateDTO stateDTO = StateDTO.builder()
                    .ruleCode(ruleInfoDTO.getRuleCode())
                    .ruleVersion(ruleInfoDTO.getRuleVersion())
                    .channel(ruleInfoDTO.getChannel())
                    .targetField(flinkEventDTO.getTargetField())
                    .targetValue(flinkEventDTO.getTargetValue())
                    .build();
            out.collect(FlinkResultDTO.builder().stateDTO(stateDTO).build());
            hasValueState.update(true);
        }
        // 状态值防空
        Tuple3<Long, FlinkEventDTO, Long> tuple3 = smallMapState.get(flinkEventDTO.getEventField());
        if (Objects.isNull(tuple3)) {
            smallMapState.put(flinkEventDTO.getEventField(), Tuple3.of(0L, flinkEventDTO, timestamp));
        }
        // 规则事件值计算
        if (ruleCondDTO.getCrossHistory()) { //跨历史时间段
            String crossHistoryTimeline = ruleCondDTO.getCrossHistoryTimeline();
            // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，
            // 所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据
            if (flinkEventDTO.getEventTime()
                    <= LocalDateTimeUtils.convertString2Timestamp(crossHistoryTimeline)) {
                return false;
            }
            // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，
            // 所以检查当前值是否已经通过redis初始化后，防止重复初始化
            if (!smallInitMapState.contains(flinkEventDTO.getEventField())) {
                // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值
                // （注意：Groovy字符串拼接的方式很麻烦，故使用StringBuilder）
                String redisKey = buildRedisKey(ruleCondDTO);
                String redisHashKey = buildRedisHashKey(flinkEventDTO);
                // 注意：因为上面获取历史缓存数据时，使用的是 <= 所以 redis 存储值时查询 doris 要包含历史截至时间点
                String initValue = RedisUtil.hget(redisKey, redisHashKey);
                RedisUtil.hdel(redisKey, redisHashKey);
                if (StringUtils.isNullOrWhitespaceOnly(initValue)) {
                    log.warn("因规则[{}]的redis初始值为空，故跳过此次计算！redisKey: {}, redisHashKey: {}, 当前事件数据：{}", ruleInfoDTO.getRuleCode(), redisKey, redisHashKey, flinkEventDTO);
                    return false;
                }
                smallMapState.put(flinkEventDTO.getEventField(), Tuple3.of(Long.parseLong(initValue), flinkEventDTO, timestamp));
                smallInitMapState.put(flinkEventDTO.getEventField(), true);
            }
            // 从redis初始化值后，正常处理数据
            addEventValue(timestamp, flinkEventDTO);
        } else { // 非跨历史时间段
            // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据
            if (flinkEventDTO.getEventTime() != timestamp) {
                return false;
            }
            addEventValue(timestamp, flinkEventDTO);
        }
        return true;
    }

    /**
     * 处理单个规则条件的匹配和规则计算逻辑
     */
    private boolean processRuleCondition(long timestamp, FlinkEventDTO flinkEventDTO, Collector<FlinkResultDTO> out, RuleCondDTO ruleCondDTO) throws Exception {
        // 事件与规则中的事件编号匹配不上，则直接跳过
        if (!Objects.equals(flinkEventDTO.getEventField(), ruleCondDTO.getEventField())) {
            // 事件编号匹配不上，则直接跳过
            return false;
        }
        // 进行事件属性匹配
        List<RuleEventAttrValueDTO> ruleEventAttrValueGroup = ruleCondDTO.getRuleEventAttrValueGroup();
        boolean eventAttributeMatchResult = matchEventAttribute(ruleEventAttrValueGroup, flinkEventDTO);
        if (!eventAttributeMatchResult) {
            // 事件属性匹配不上，则直接跳过
            return false;
        }

        // ******************************* 规则匹配成功，进行后续处理 *******************************

        // 规则状态历史的记录数据
        Boolean hasState = hasValueState.value();
        if (Objects.isNull(hasState) || !hasState) {
            StateDTO stateDTO = StateDTO.builder()
                    .ruleCode(ruleInfoDTO.getRuleCode())
                    .ruleVersion(ruleInfoDTO.getRuleVersion())
                    .channel(ruleInfoDTO.getChannel())
                    .targetField(flinkEventDTO.getTargetField())
                    .targetValue(flinkEventDTO.getTargetValue())
                    .build();
            out.collect(FlinkResultDTO.builder().stateDTO(stateDTO).build());
            hasValueState.update(true);
        }
        // 状态值防空
        Tuple3<Long, FlinkEventDTO, Long> tuple3 = smallMapState.get(flinkEventDTO.getEventField());
        if (Objects.isNull(tuple3)) {
            smallMapState.put(flinkEventDTO.getEventField(), Tuple3.of(0L, flinkEventDTO, timestamp));
        }
        // 规则事件值计算
        if (ruleCondDTO.getCrossHistory()) { //跨历史时间段
            String crossHistoryTimeline = ruleCondDTO.getCrossHistoryTimeline();
            // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，
            // 所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据
            if (flinkEventDTO.getEventTime()
                    <= LocalDateTimeUtils.convertString2Timestamp(crossHistoryTimeline)) {
                return false;
            }
            // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，
            // 所以检查当前值是否已经通过redis初始化后，防止重复初始化
            if (!smallInitMapState.contains(flinkEventDTO.getEventField())) {
                // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值
                // （注意：Groovy字符串拼接的方式很麻烦，故使用StringBuilder）
                String redisKey = buildRedisKey(ruleCondDTO);
                String redisHashKey = buildRedisHashKey(flinkEventDTO);
                // 注意：因为上面获取历史缓存数据时，使用的是 <= 所以 redis 存储值时查询 doris 要包含历史截至时间点
                String initValue = RedisUtil.hget(redisKey, redisHashKey);
                RedisUtil.hdel(redisKey, redisHashKey);
                if (StringUtils.isNullOrWhitespaceOnly(initValue)) {
                    log.warn("因规则[{}]的redis初始值为空，故跳过此次计算！redisKey: {}, redisHashKey: {}, 当前事件数据：{}", ruleInfoDTO.getRuleCode(), redisKey, redisHashKey, flinkEventDTO);
                    return false;
                }
                smallMapState.put(flinkEventDTO.getEventField(), Tuple3.of(Long.parseLong(initValue), flinkEventDTO, timestamp));
                smallInitMapState.put(flinkEventDTO.getEventField(), true);
            }
            // 从redis初始化值后，正常处理数据
            addEventValue(timestamp, flinkEventDTO);
        } else { // 非跨历史时间段
            // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据
            if (flinkEventDTO.getEventTime() != timestamp) {
                return false;
            }
            addEventValue(timestamp, flinkEventDTO);
        }
        return true;
    }

    private void addEventValue(long timestamp, FlinkEventDTO flinkEventDTO) throws Exception {
        Tuple3<Long, FlinkEventDTO, Long> tuple3 = smallMapState.get(flinkEventDTO.getEventField());
        Long currentValue = tuple3.f0;
        Long newValue = currentValue + Long.parseLong(flinkEventDTO.getEventValue());
        smallMapState.put(flinkEventDTO.getEventField(), Tuple3.of(newValue, flinkEventDTO, timestamp));
    }


    /**
     * 构建Redis的哈希键
     */
    private String buildRedisHashKey(FlinkEventDTO flinkEventDTO) {
        String targetField = flinkEventDTO.getTargetField();
        String targetValue = flinkEventDTO.getTargetValue();
        return targetField +
                RedisKeyConstants.REDIS_KEY_SPLIT +
                targetValue;
    }

    /**
     * 构建Redis的key
     */
    private String buildRedisKey(RuleCondDTO ruleCondDTO) {
        return RedisKeyConstants.DORIS_EVENT_HISTORY_VALUE +
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
     * @param flinkEventDTO Kafka事件DTO，包含Kafka事件的详细信息，包括事件属性
     * @return boolean 如果Kafka事件属性与规则事件属性完全匹配，则返回true；否则返回false
     */
    private boolean matchEventAttribute(List<RuleEventAttrValueDTO> ruleEventAttrValueGroup, FlinkEventDTO flinkEventDTO) {
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
            Map<String, String> kafkaEventAttrMap = flinkEventDTO.getEventAttrMap();
            if (Objects.isNull(kafkaEventAttrMap) || kafkaEventAttrMap.isEmpty()) {
                // 规则包含事件属性配置，但是kafka数据事件属性map为空，故直接判定为不符合规则要求
                log.warn("规则包含事件属性配置，但是kafka数据事件属性map为空，故直接判定为不符合规则要求！" +
                        "规则事件属性信息:{}, 当前事件信息:{}", ruleEventAttrValueDTO, flinkEventDTO);
                return false;
            }
            if (!kafkaEventAttrMap.containsKey(attrField)) {
                // kafka事件属性不包含规则中事件属性，则表明不符合匹配
                log.warn("kafka数据事件属性map并不包含规则配置的事件属性Field，故直接判定为不符合规则要求！" +
                        "规则事件属性信息:{}, 当前事件信息:{}", ruleEventAttrValueDTO, flinkEventDTO);
                return false;
            }
            String kafkaEventAttributeValue = kafkaEventAttrMap.get(attrField);
            if (Objects.isNull(kafkaEventAttributeValue)) {
                // kafka事件中对于规则中事件属性值为空，则表明不符合匹配
                return false;
            }
            // 比较kafka中属性值与规则中属性值
            boolean isMatch = RuleEventAttrCompUtil.compareValues(ruleEventAttrValueDTO, flinkEventDTO);
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
     * @param timestamp 处理时间戳
     * @param out       输出收集器，用于收集和输出预警信息
     * @throws Exception 可能抛出的异常
     */
    @Override
    public boolean onTimer(KeyedBroadcastProcessFunction.ReadOnlyContext ctx, long timestamp, Collector<FlinkResultDTO> out) throws Exception {
        if (Objects.isNull(ruleInfoDTO)) {
            log.warn("因规则信息为空，故跳过此次计算！");
            return false;
        }
        // 获取规则条件
        List<RuleCondDTO> ruleCondGroup = ruleInfoDTO.getRuleCondGroup();
        if (ruleCondGroup == null || ruleCondGroup.isEmpty()) {
            log.warn("因规则[{}]的条件组为空，故跳过此次计算！", ruleInfoDTO.getRuleCode());
            return false;
        }
        // 将规则条件根据事件编号存储到map中，方便后续操作
        Map<String, RuleCondDTO> ruleConditionMapByEventField = new HashMap<>();
        for (RuleCondDTO ruleCondDTO : ruleCondGroup) {
            ruleConditionMapByEventField.put(ruleCondDTO.getEventField(), ruleCondDTO);
        }
        // 获取并效验条件类型
        String condType = getCondType(ruleConditionMapByEventField);
        if (condType == null) {
            return false;
        }
        // 数据计算，返回定时器是否注册判断
        if (Objects.equals(condType, RuleCondTypeEnum.RECENT.getCode())) { // 最近时间类型
            return onTimerRecent(ctx, timestamp, out, condType, ruleConditionMapByEventField);
        } else if (Objects.equals(condType, RuleCondTypeEnum.RANGE.getCode())) { // 范围时间类型
            onTimerRange(timestamp);
            return false;
        } else {
            log.warn("因规则[{}]中事件条件类型为未知值[{}]，故跳过此次计算！", ruleInfoDTO.getRuleCode(), condType);
            return false;
        }
    }

    /**
     * 处理范围时间类型规则计算
     */
    private void onTimerRange(long timestamp) throws Exception {
        for (Map.Entry<String, Long> entry : nextEndTimestampState.entries()) {
            String key = entry.getKey();
            Long value = entry.getValue();
            if (value == null) {
                return;
            }
            if (value == timestamp) {
                smallMapState.remove(key);
                nextEndTimestampState.remove(key);
                inTimeRangeMapState.remove(key);
                latestEventThresholdMapState.remove(key);
            }
        }
    }

    /**
     * 处理最近时间类型规则计算
     */
    private boolean onTimerRecent(KeyedBroadcastProcessFunction.ReadOnlyContext ctx, long timestamp,
                                  Collector<FlinkResultDTO> out, String condType,
                                  Map<String, RuleCondDTO> ruleConditionMapByEventField) throws Exception {
//        boolean debug = Objects.equals(ruleInfoDTO.getRuleCode(), 1895313318898438144L);
//        if (debug) {
//            logSmallMapState(ruleInfoDTO.getRuleCode(), (String) ctx.getCurrentKey());
//        }
        // 将小时间窗口（步长窗口）中的数据累加到大时间窗口（整体窗口）中，并返回最新（时间戳最大）的事件数据。
        aggregateSmallMapToBigMap(timestamp);
//        if (debug) {
//            logBigMapState(ruleInfoDTO.getRuleCode(), (String) ctx.getCurrentKey());
//        }
        // 清理窗口大小之外的数据
        cleanupWindowData(timestamp, ruleConditionMapByEventField);
        // 处理bigMapState
        Tuple2<Boolean, ProcessorDTO> processBigMapResult = processBigMap(ruleConditionMapByEventField, ruleInfoDTO.getRuleCondCombOp());
        // 处理预警结果
        handleAlertResult(ctx, timestamp, out, condType, processBigMapResult);
        return hasActiveEvents();
    }

    private String getCondType(Map<String, RuleCondDTO> ruleConditionMapByEventField) {
        String condType = null;
        for (Map.Entry<String, RuleCondDTO> entry : ruleConditionMapByEventField.entrySet()) {
            RuleCondDTO ruleCondDto = entry.getValue();
            if (Objects.isNull(condType)) {
                condType = ruleCondDto.getCondType();
            } else if (!condType.equals(ruleCondDto.getCondType())) {
                log.warn("规则[{}]中多个事件条件类型不一致，故跳过此次计算！", ruleInfoDTO.getRuleCode());
                return null;
            }
        }
        return condType;
    }

    private void logSmallMapState(Long ruleCode, String currentKey) throws Exception {
        Map<String, Tuple3<Long, FlinkEventDTO, Long>> smallMap = new HashMap<>();
        for (Map.Entry<String, Tuple3<Long, FlinkEventDTO, Long>> entry : smallMapState.entries()) {
            smallMap.put(entry.getKey(), entry.getValue());
        }
        log.info("smallMap：{}, ruleCode:{}, currentKey：{}", JsonUtils.toJsonString(smallMap), ruleCode, currentKey);
    }

    private void logBigMapState(Long ruleCode, String currentKey) throws Exception {
        Map<Tuple2<String, Long>, Long> bigMap = new HashMap<>();
        for (Map.Entry<Tuple2<String, Long>, Long> entry : bigMapState.entries()) {
            bigMap.put(entry.getKey(), entry.getValue());
        }
        log.info("bigMap：{}, ruleCode:{}, currentKey：{}", JsonUtils.toJsonString(bigMap), ruleCode, currentKey);
    }

//    private void logOldState(Long ruleCode, String currentKey) throws Exception {
//        Map<Tuple2<String, Long>, Tuple2<Long, Long>> bigMap = new HashMap<>();
//        for (Map.Entry<Tuple2<String, Long>, Tuple2<Long, Long>> entry : oldBigMapState.entries()) {
//            bigMap.put(entry.getKey(), entry.getValue());
//        }
//        log.debug("残留旧状态，ruleCode:{}, currentKey：{}, bigMap：{}", ruleCode, currentKey, bigMap);
//    }

    /**
     * 构建预警信息的方法，提取重复逻辑
     */
    private AlertDTO buildAlert(long timestamp, RuleInfoDTO ruleInfoDTO, FlinkEventDTO latestFlinkEventDTO,
                                ProcessorDTO processorDTO) {
        String finalWarnMessage = TemplateUtil.replacePlaceholders(
                ruleInfoDTO.getAlertTemplate(),
                ruleInfoDTO,
                latestFlinkEventDTO,
                processorDTO
        );
        return AlertDTO.builder()
                .channel(ruleInfoDTO.getChannel())
                .ruleCode(ruleInfoDTO.getRuleCode())
                .message(finalWarnMessage)
                .time(LocalDateTimeUtils.convertTimestamp2String(timestamp))
                .targetField(ruleInfoDTO.getTargetField())
                .targetValue(latestFlinkEventDTO.getTargetValue())
                .eventValueGroup(processorDTO.getEventValueGroup())
                .build();
    }

    /**
     * 根据规则信息获取告警间隔时间（以毫秒为单位）。
     */
    private Long getAlertInterval(RuleInfoDTO ruleInfoDTO) {
        Long alertIntervalValue = ruleInfoDTO.getAlertIntervalValue();
        String alertIntervalUnit = ruleInfoDTO.getAlertIntervalUnit();
        if (Objects.isNull(alertIntervalValue) || Objects.isNull(alertIntervalUnit)) {
            return null;
        }
        return TimeUtil.toMillis(alertIntervalValue, TimeUnitEnum.fromEnUnit(alertIntervalUnit));
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
     * @param ruleCondCombOp               规则条件的组合操作符，用于确定如何组合多个规则条件的结果
     * @return 返回一个Tuple2对象，包含事件结果、处理器DTO
     * @throws Exception 如果处理过程中发生错误，则抛出异常
     */
    private Tuple2<Boolean, ProcessorDTO> processBigMap(Map<String, RuleCondDTO> ruleConditionMapByEventField,
                                                        String ruleCondCombOp) throws Exception {
        // 获取事件与之判断结果
        Map<String, Boolean> eventFieldAndAlertResult = new HashMap<>();
        // 获取事件字段与值之和
        Map<String, Long> eventFiledAndValueSumMap = new HashMap<>();
        // 遍历 MapState 的所有条目
        for (Map.Entry<Tuple2<String, Long>, Long> entry : bigMapState.entries()) {
            Tuple2<String, Long> key = entry.getKey(); // 获取键，包含 eventField 和关联的时间戳值
            Long eventValue = entry.getValue(); // 获取事件累加值
            String eventField = key.f0; // Tuple2 的第一个元素作为事件字段
            // 使用 merge 方法高效地累加值
            eventFiledAndValueSumMap.merge(eventField, eventValue, Long::sum);
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
            // 获取事件字段与其对应的预警结果
            boolean alertResult = calcAlertResult(eventField, eventValueSum, ruleCondDTO.getThreshold(), ruleCondDTO.getThresholdScaleFactor());
            eventFieldAndAlertResult.put(eventField, alertResult);
        }
        boolean eventResult = evaluateEventResults(eventFieldAndAlertResult, ruleCondCombOp);
        // 构建运算机的DTO对象
        ProcessorDTO processorDTO = ProcessorDTO.builder()
                .eventValueGroup(eventFiledAndValueSumMap)
                .build();
        return Tuple2.of(eventResult, processorDTO);
    }

    /**
     * 计算规则条件是否满足，并返回结果
     * 此方法用于判断当前事件值的总和是否超过了某个阈值，并根据条件更新该阈值
     * 主要用于动态调整事件的敏感度，以适应不同的业务需求
     */
    private Boolean calcAlertResult(String eventField, Long eventValueSum,
                                    Long eventThreshold, Long thresholdScaleFactor) throws Exception {
        boolean result = false;
        if (Objects.isNull(thresholdScaleFactor)) {
            log.warn("因规则[{}]的缩放因子为空，故跳过此次计算！", ruleInfoDTO.getRuleCode());
        } else {
            Long latestThreshold = latestEventThresholdMapState.get(eventField);
            if (Objects.isNull(latestThreshold)) {
                latestThreshold = eventThreshold;
            }
            if (eventValueSum > latestThreshold) {
                latestThreshold = latestThreshold * thresholdScaleFactor;
                result = true;
            }
            latestEventThresholdMapState.put(eventField, latestThreshold);
        }
        return result;
    }


    /**
     * 将小时间窗口（步长窗口）中的数据累加到大时间窗口（整体窗口）中，并更新最新（时间戳最大）的事件数据。
     */
    private void aggregateSmallMapToBigMap(long timestamp) throws Exception {
        // 遍历 smallMapState 的所有条目
        Long timestampMax = 0L;
        for (Map.Entry<String, Tuple3<Long, FlinkEventDTO, Long>> smallMapEntry : smallMapState.entries()) {
            String eventField = smallMapEntry.getKey();
            Tuple3<Long, FlinkEventDTO, Long> tuple3 = smallMapEntry.getValue();
            // 创建新的 Tuple2 作为 bigMapState 的键
            Tuple2<String, Long> tupleKey = Tuple2.of(eventField, timestamp);
            // 将 (eventField, timestamp) 作为键，eventValue 作为值，存入 bigMapState
            Long oldValue = bigMapState.get(tupleKey);
            if (Objects.isNull(oldValue)) {
                oldValue = 0L;
            }
            bigMapState.put(tupleKey, oldValue + tuple3.f0);
            // 更新最新的事件数据
            Long eventTime = tuple3.f2;
            if (eventTime > timestampMax) {
                timestampMax = eventTime;
                lastEventState.update(tuple3.f1);
            }
        }
        // 当前窗口步长的数据已经添加到窗口中了，清空当前key状态
        smallMapState.clear();
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
        Iterator<Map.Entry<Tuple2<String, Long>, Long>> iterator = bigMapState.entries().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Tuple2<String, Long>, Long> stateEntry = iterator.next();
            Tuple2<String, Long> eventFieldAndTimeTuple2 = stateEntry.getKey();
            String eventField = eventFieldAndTimeTuple2.f0;
            Long eventTime = eventFieldAndTimeTuple2.f1;

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
            log.warn("因规则[{}]的条件组合操作符非法，故跳过此次计算！", ruleInfoDTO.getRuleCode());
            return false;
        }

        return result;
    }
}
