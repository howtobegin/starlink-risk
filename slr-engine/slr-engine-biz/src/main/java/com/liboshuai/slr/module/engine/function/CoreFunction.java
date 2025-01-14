package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.number.WindowUtil;
import com.liboshuai.slr.module.engine.dto.*;
import com.liboshuai.slr.module.engine.framework.exception.BusinessException;
import com.liboshuai.slr.module.engine.processor.Processor;
import com.liboshuai.slr.module.engine.processor.impl.ProcessorOne;
import groovy.lang.GroovyClassLoader;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.util.Collector;
import org.apache.flink.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.liboshuai.slr.module.engine.framework.state.StateDescContainer.*;

/**
 * 计算引擎核心function
 */
@Slf4j
public class CoreFunction extends KeyedBroadcastProcessFunction<String, KafkaEventDTO, RuleCdcDTO, ResultDTO> {

    private static final long serialVersionUID = -5913085790319815064L;

    /**
     * 规则运算机池：key-规则编号，value-运算机对象
     */
    private Map<String, Processor> ruleProcessorPool;
    /**
     * groovy加载器
     */
    private GroovyClassLoader groovyClassLoader;
    /**
     * 最近5分钟时间事件数据缓存
     */
    private Map<String, List<KafkaEventDTO>> recentEventMap;
    /**
     * 旧规则列表
     */
    private MapState<String, Void> oldRuleListState;

    // 上一个同规则的运算机残留状态
    private MapState<String, Boolean> smallInitMapState;
    private ValueState<Long> lastWarningTimeState;
    private MapState<String, Long> latestEventThresholdMapState;
    private MapState<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>> bigMapState;

    /**
     * 注意千万不要在open方法中对状态进行赋值操作，因为在processElement等方法中并不能获取到
     */
    @Override
    public void open(Configuration parameters) {
        ruleProcessorPool = new ConcurrentHashMap<>();
        groovyClassLoader = new GroovyClassLoader();
        RECENT_EVENT_MAP_STATE_DESC
                .enableTimeToLive(StateTtlConfig.newBuilder(Time.minutes(2)).neverReturnExpired().build());
        recentEventMap = new HashMap<>();
        oldRuleListState = getRuntimeContext().getMapState(OLD_RULE_MAP_STATE_DESC);
    }

    @Override
    public void processElement(KafkaEventDTO kafkaEventDTO,
                               KeyedBroadcastProcessFunction<String, KafkaEventDTO, RuleCdcDTO, ResultDTO>.ReadOnlyContext ctx,
                               Collector<ResultDTO> out) throws Exception {
        // 获取当前key
        String currentKey = ctx.getCurrentKey();
        // 获取下线规则key信息
        RuleKeyHistoryDTO ruleKeyHistoryDTO = kafkaEventDTO.getRuleKeyHistoryDTO();
        if (Objects.nonNull(ruleKeyHistoryDTO)) {
            clearOldState(ruleKeyHistoryDTO);
            return;
        }
        // 设置事件时间为Flink当前处理时间（注意：设置时间事件一定要放在缓存列表之前）
        long currentProcessingTime = ctx.timerService().currentProcessingTime();
        kafkaEventDTO.setEventTime(currentProcessingTime);
        // 将设置了事件时间的数据放入结果中，以便后续写入doris
        ResultDTO resultDTO = ResultDTO.builder()
                .kafkaEventDTO(kafkaEventDTO)
                .build();
        out.collect(resultDTO);
        // 将事件添加到缓存列表中并移除超过5分钟的过期数据
        addEventToCacheAndRemoveExpired(currentKey, kafkaEventDTO, currentProcessingTime);
        // 从广播流中获取规则信息
        ReadOnlyBroadcastState<String, RuleInfoDTO> broadcastState = ctx.getBroadcastState(BROADCAST_RULE_MAP_STATE_DESC);
        // 数据遍历经过每个规则运算机
        for (Map.Entry<String, Processor> stringProcessorEntry : ruleProcessorPool.entrySet()) {
            String ruleCode = stringProcessorEntry.getKey();
            Processor processor = stringProcessorEntry.getValue();
            if (!oldRuleListState.contains(ruleCode)) {
                // 新规则需要先将缓存的最近历史事件数据处理一遍
                List<KafkaEventDTO> historyKafkaEventDTOList = recentEventMap.get(currentKey);
                if (CollectionUtil.isNullOrEmpty(historyKafkaEventDTOList)) {
                    continue;
                }
                for (KafkaEventDTO historyKafkaEventDto : historyKafkaEventDTOList) {
                    processor.processElement(currentKey, currentProcessingTime, broadcastState.get(ruleCode), historyKafkaEventDto, out);
                }
                oldRuleListState.put(ruleCode, null);
            } else {
                // 否则直接处理当前一条事件数据即可
                processor.processElement(currentKey, currentProcessingTime, broadcastState.get(ruleCode), kafkaEventDTO, out);
            }
        }
        // 注册定时器（窗口大小1分钟）
        long fireTime = WindowUtil.getWindowStartWithOffset(currentProcessingTime, 0, 60 * 1000) + 60 * 1000;
        ctx.timerService().registerProcessingTimeTimer(fireTime);
    }

    /**
     * 将事件添加到缓存列表中并移除超过5分钟的过期数据。
     *
     * @param currentKey            当前的键值
     * @param kafkaEventDTO         要添加的事件对象
     * @param currentProcessingTime 当前的处理时间
     */
    private void addEventToCacheAndRemoveExpired(String currentKey, KafkaEventDTO kafkaEventDTO, long currentProcessingTime) {
        // 将事件放入缓存列表中
        recentEventMap
                .computeIfAbsent(currentKey, k -> new ArrayList<>())
                .add(kafkaEventDTO);
        List<KafkaEventDTO> kafkaEventDTOList = recentEventMap.get(currentKey);
        // 遍历并移除过期的数据
        Iterator<KafkaEventDTO> iterator = kafkaEventDTOList.iterator();
        while (iterator.hasNext()) {
            KafkaEventDTO eventDTO = iterator.next();
            Long eventTime = eventDTO.getEventTime();
            if (eventTime < currentProcessingTime - TimeUnit.MINUTES.toMillis(5)) {
                // 移除过期数据
                iterator.remove();
            }
        }
    }

    private void clearOldState(RuleKeyHistoryDTO ruleKeyHistoryDTO) throws Exception {
        Long ruleCode = ruleKeyHistoryDTO.getRuleCode();
        Long ruleVersion = ruleKeyHistoryDTO.getRuleVersion();

        RuntimeContext runtimeContext = getRuntimeContext();
        // 状态变量注册使用 ruleCode + ruleVersion 作为后缀，以防止不同规则使用相同的模型导致状态变量数据冲突覆盖
        String smallInitMapStateName = "smallInitMapState_" + ruleCode + "_" + ruleVersion;
        smallInitMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(smallInitMapStateName, Types.STRING, Types.BOOLEAN)
        );
        String lastWarningTimeStateName = "lastWarningTimeState_" + ruleCode + "_" + ruleVersion;
        lastWarningTimeState = runtimeContext.getState(
                new ValueStateDescriptor<>(lastWarningTimeStateName, Types.LONG)
        );
        String latestEventThresholdMapStateName = "latestEventThresholdMapStateName_" + ruleCode + "_" + ruleVersion;
        latestEventThresholdMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(latestEventThresholdMapStateName, Types.STRING, Types.LONG)
        );
        String bigMapStateName = "bigMapState_" + ruleCode + "_" + ruleVersion;
        bigMapState = runtimeContext.getMapState(
                new MapStateDescriptor<>(bigMapStateName, Types.TUPLE(Types.STRING, Types.LONG),
                        Types.TUPLE(Types.LONG, Types.POJO(KafkaEventDTO.class)))
        );
//        logState("之前");
        smallInitMapState.clear();
        lastWarningTimeState.clear();
        latestEventThresholdMapState.clear();
        bigMapState.clear();
//        logState("之后");
    }

    /**
     * 打印状态值
     */
    private void logState(String status) throws Exception {
        Map<String, Boolean> smallInitMap = new HashMap<>();
        Iterator<Map.Entry<String, Boolean>> oldSmallInitMapIterator = smallInitMapState.iterator();
        while (oldSmallInitMapIterator.hasNext()) {
            Map.Entry<String, Boolean> next = oldSmallInitMapIterator.next();
            smallInitMap.put(next.getKey(), next.getValue());
        }

        Long lastWarningTime = lastWarningTimeState.value();

        Map<String, Long> latestEventThresholdMap = new HashMap<>();
        Iterator<Map.Entry<String, Long>> latestEventThresholdMapStateIterator = latestEventThresholdMapState.iterator();
        while (latestEventThresholdMapStateIterator.hasNext()) {
            Map.Entry<String, Long> next = latestEventThresholdMapStateIterator.next();
            latestEventThresholdMap.put(next.getKey(), next.getValue());
        }

        Map<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>> bigMap = new HashMap<>();
        Iterator<Map.Entry<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>>> oldBigMapStateIterator = bigMapState.iterator();
        while (oldBigMapStateIterator.hasNext()) {
            Map.Entry<Tuple2<String, Long>, Tuple2<Long, KafkaEventDTO>> next = oldBigMapStateIterator.next();
            bigMap.put(next.getKey(), next.getValue());
        }

        log.warn("========================================清理状态值-{}========================================", status);
        log.warn("smallInitMap: {}", JsonUtils.toJsonString(smallInitMap));
        log.warn("lastWarningTime: {}", JsonUtils.toJsonString(lastWarningTime));
        log.warn("latestEventThresholdMap: {}", JsonUtils.toJsonString(latestEventThresholdMap));
        log.warn("bigMap: {}", JsonUtils.toJsonString(bigMap));
        log.warn("========================================清理状态值-{}========================================", status);
    }

    @Override
    public void processBroadcastElement(RuleCdcDTO ruleCdcDTO,
                                        KeyedBroadcastProcessFunction<String, KafkaEventDTO, RuleCdcDTO, ResultDTO>.Context ctx,
                                        Collector<ResultDTO> out) throws Exception {
        if (ruleCdcDTO == null) {
            throw new BusinessException("Mysql Cdc 广播流 ruleCdcDTO 必须非空");
        }
        // cdc 数据变更类型
        String op = ruleCdcDTO.getOp();
        // 变更之前的数据
        RuleJsonDTO ruleCdcDTOBefore = ruleCdcDTO.getBefore();
        String ruleCodeBefore = ruleCdcDTOBefore.getRuleCode();
        // 变更之后的数据
        RuleJsonDTO ruleCdcDTOAfter = ruleCdcDTO.getAfter();
        String ruleCodeAfter = ruleCdcDTOAfter.getRuleCode();
        String ruleJsonAfter = ruleCdcDTOAfter.getRuleJson();
        RuleInfoDTO ruleInfoDTOAfter = JsonUtils.parseObject(ruleJsonAfter, RuleInfoDTO.class);
        // 获取广播流数据
        BroadcastState<String, RuleInfoDTO> broadcastState = ctx.getBroadcastState(BROADCAST_RULE_MAP_STATE_DESC);
        // 上下线规则运算机
        if (Envelope.Operation.CREATE.code().equals(op)) {
            // create: 只有发布上线规则的时候，才会出现创建操作，所以需要加载规则运算机
            loadProcessor(ruleCodeAfter, broadcastState, ruleInfoDTOAfter);
        } else if (Envelope.Operation.READ.code().equals(op)) {
            // read: 读操作意味着计算引擎是刚刚启动，我们需要从数据库中恢复加载之前已经上线的规则运算机
            loadProcessor(ruleCodeAfter, broadcastState, ruleInfoDTOAfter);
        } else if (Envelope.Operation.UPDATE.code().equals(op)) {
            // update: 因为上线规则时进行insert，下线规则时直接delete了，其他更新操作不会同步到rule_json表中，故忽略
            log.warn("规则运算机不支持在线热更新，请不要直接 update rule_json 表中的数据！");
        } else if (Envelope.Operation.DELETE.code().equals(op)) {
            // delete: 删除操作顾名思义，就是执行了下线操作，这个时候，我们只需要将规则运算机移除即可
            removeProcessor(ruleCodeBefore, broadcastState);
        }
        log.warn("当前规则运算机数量: {}, 规则编号列表: {}", ruleProcessorPool.size(), ruleProcessorPool.keySet());
    }

    /**
     * 移除规则运算机
     */
    private void removeProcessor(String ruleCode, BroadcastState<String, RuleInfoDTO> broadcastState) throws Exception {
        Processor processor = ruleProcessorPool.get(ruleCode);
        if (Objects.isNull(processor)) {
            log.warn("规则运算机不存在，无需移除，规则编号为: {}", ruleCode);
            return;
        }
        ruleProcessorPool.remove(ruleCode);
        broadcastState.remove(ruleCode);
        log.warn("下线一个规则运算机，规则编号为: {}", ruleCode);
    }

    /**
     * 加载规则运算机
     */
    private void loadProcessor(String ruleCode, BroadcastState<String, RuleInfoDTO> broadcastState,
                               RuleInfoDTO ruleInfoDTO) throws Exception {
        if (ruleProcessorPool.containsKey(ruleCode)) {
            log.warn("规则运算机已存在，无需再次加载，规则编号为: {}", ruleCode);
            return;
        }
        // 构建规则运算机
        Processor processor = mockProcessor(getRuntimeContext(), ruleInfoDTO);
        ruleProcessorPool.put(ruleCode, processor);
        broadcastState.put(ruleCode, ruleInfoDTO);
        log.warn("上线一个规则运算机，规则编号为: {}", ruleCode);
    }

    /**
     * 由于每个 key 都独立维护自己的计时器状态，若两个不同的 key 在相同的时间点触发了计时器，则 onTimer 方法会被调用两次。
     */
    @Override
    public void onTimer(long timestamp,
                        KeyedBroadcastProcessFunction<String, KafkaEventDTO, RuleCdcDTO, ResultDTO>.OnTimerContext ctx,
                        Collector<ResultDTO> out) throws Exception {
        // 获取当前Key
        String currentKey = ctx.getCurrentKey();
        // 从广播流中获取规则信息
        ReadOnlyBroadcastState<String, RuleInfoDTO> broadcastState = ctx.getBroadcastState(BROADCAST_RULE_MAP_STATE_DESC);
        // 判断当前key所有运算机中是否有待处理的定时器
        boolean hasPendingTimers = false;
        // 数据遍历经过每个规则运算机
        for (Map.Entry<String, Processor> stringProcessorEntry : ruleProcessorPool.entrySet()) {
            String ruleCode = stringProcessorEntry.getKey();
            Processor processor = stringProcessorEntry.getValue();
            // 调用定时器
            boolean hasActiveEvents = processor.onTimer(currentKey, timestamp, broadcastState.get(ruleCode), out);
            if (hasActiveEvents) {
                hasPendingTimers = true;
            }
        }
        // 如果运算机中有待处理的定时器，则注册下一次flink定时器。
        if (hasPendingTimers) {
            // 注册下一次输出累积值的Timer。该timestamp就是窗口结束时刻，下一个窗口可以直接加60s。
            long nextTimerTime = timestamp + 60 * 1000;
            ctx.timerService().registerProcessingTimeTimer(nextTimerTime);
        }
    }

    /**
     * 构造运算机对象
     */
    private Processor buildProcessor(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) throws Exception {
        String ruleModelGroovyCode = ruleInfoDTO.getModelGroovy();
        if (StringUtils.isNullOrWhitespaceOnly(ruleModelGroovyCode)) {
            throw new BusinessException("运算机模型代码 ruleModelGroovyCode 必须非空");
        }
        Class aClass = groovyClassLoader.parseClass(ruleModelGroovyCode);
        Processor processor = (Processor) aClass.newInstance();
        processor.init(runtimeContext, ruleInfoDTO);
        return processor;
    }

    // mock运算机对象
    private Processor mockProcessor(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) throws Exception {
        Processor processor = new ProcessorOne();
        processor.init(runtimeContext, ruleInfoDTO);
        return processor;
    }

}