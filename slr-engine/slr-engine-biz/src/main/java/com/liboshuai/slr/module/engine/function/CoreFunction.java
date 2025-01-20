package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.number.WindowUtil;
import com.liboshuai.slr.module.engine.dto.*;
import com.liboshuai.slr.module.engine.framework.exception.BusinessException;
import com.liboshuai.slr.module.engine.framework.state.CommonStateDesc;
import com.liboshuai.slr.module.engine.framework.state.ProcessorOneStateDesc;
import com.liboshuai.slr.module.engine.processor.Processor;
import groovy.lang.GroovyClassLoader;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.KeyedStateStore;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.util.Collector;
import org.apache.flink.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 计算引擎核心function
 */
@Slf4j
public class CoreFunction extends KeyedBroadcastProcessFunction<String, KafkaEventDTO, MysqlCdcDTO, ResultDTO> implements CheckpointedFunction {

    private static final long serialVersionUID = -5913085790319815064L;

    /**
     * 规则信息池：key-规则编号，value-规则信息对象（用于广播流）
     */
    private final Map<Long, RuleInfoDTO> ruleInfoPool = new ConcurrentHashMap<>();
    /**
     * 规则运算机池：key-规则编号，value-运算机对象
     */
    private final Map<Long, Processor> ruleProcessorPool = new ConcurrentHashMap<>();
    /**
     * 最近5分钟时间事件数据缓存
     */
    private final Map<String, List<KafkaEventDTO>> recentEventMap = new HashMap<>();
    /**
     * groovy加载器
     */
    private GroovyClassLoader groovyClassLoader;
    /**
     * 规则信息list状态（用于故障恢复）
     */
    private ListState<RuleInfoDTO> restoreRuleInfoListState;
    /**
     * 旧规则列表
     */
    private MapState<Long, Void> oldRuleListState;

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
        groovyClassLoader = new GroovyClassLoader();
        RuntimeContext runtimeContext = getRuntimeContext();
        oldRuleListState = runtimeContext.getMapState(CommonStateDesc.OLD_RULE_MAP_STATE_DESC);
    }

    @Override
    public void processElement(KafkaEventDTO kafkaEventDTO,
                               KeyedBroadcastProcessFunction<String, KafkaEventDTO, MysqlCdcDTO, ResultDTO>.ReadOnlyContext ctx,
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
        // 数据遍历经过每个规则运算机
        for (Map.Entry<Long, Processor> stringProcessorEntry : ruleProcessorPool.entrySet()) {
            Long ruleCode = stringProcessorEntry.getKey();
            Processor processor = stringProcessorEntry.getValue();
            if (!oldRuleListState.contains(ruleCode)) {
                // 新规则需要先将缓存的最近历史事件数据处理一遍
                List<KafkaEventDTO> historyKafkaEventDTOList = recentEventMap.get(currentKey);
                if (CollectionUtil.isNullOrEmpty(historyKafkaEventDTOList)) {
                    continue;
                }
                for (KafkaEventDTO historyKafkaEventDto : historyKafkaEventDTOList) {
                    processor.processElement(currentKey, currentProcessingTime, historyKafkaEventDto, out);
                }
                oldRuleListState.put(ruleCode, null);
            } else {
                // 否则直接处理当前一条事件数据即可
                processor.processElement(currentKey, currentProcessingTime, kafkaEventDTO, out);
            }
        }
        // 注册定时器（窗口大小1分钟）
        long fireTime = WindowUtil.getWindowStartWithOffset(currentProcessingTime, 0, TimeUnit.MINUTES.toMillis(1))
                + TimeUnit.MINUTES.toMillis(1);
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
        smallInitMapState = runtimeContext.getMapState(ProcessorOneStateDesc.getSmallInitMapStateDesc(ruleCode, ruleVersion));
        lastWarningTimeState = runtimeContext.getState(ProcessorOneStateDesc.getLastWarningTimeStateDesc(ruleCode, ruleVersion));
        latestEventThresholdMapState = runtimeContext.getMapState(ProcessorOneStateDesc.getLatestEventThresholdMapStateDesc(ruleCode, ruleVersion));
        bigMapState = runtimeContext.getMapState(ProcessorOneStateDesc.getGigMapStateDesc(ruleCode, ruleVersion));

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

        log.info("========================================清理状态值-{}========================================", status);
        log.info("smallInitMap: {}", JsonUtils.toJsonString(smallInitMap));
        log.info("lastWarningTime: {}", JsonUtils.toJsonString(lastWarningTime));
        log.info("latestEventThresholdMap: {}", JsonUtils.toJsonString(latestEventThresholdMap));
        log.info("bigMap: {}", JsonUtils.toJsonString(bigMap));
        log.info("========================================清理状态值-{}========================================", status);
    }

    @Override
    public void processBroadcastElement(MysqlCdcDTO mysqlCdcDTO,
                                        KeyedBroadcastProcessFunction<String, KafkaEventDTO, MysqlCdcDTO, ResultDTO>.Context ctx,
                                        Collector<ResultDTO> out) throws Exception {
        if (mysqlCdcDTO == null) {
            log.warn("规则运算机上下线处理失败，MysqlCdc规则信息ruleCdcDTO必须非空！");
            return;
        }
        // cdc 数据变更类型
        String op = mysqlCdcDTO.getOp();
        // 变更之前的数据
        RuleJsonDTO ruleCdcDTOBefore = JsonUtils.parseObject(mysqlCdcDTO.getBefore(), RuleJsonDTO.class);
        if (Objects.isNull(ruleCdcDTOBefore)) {
            ruleCdcDTOBefore = new RuleJsonDTO();
        }
        Long ruleCodeBefore = ruleCdcDTOBefore.getRuleCode();
        // 变更之后的数据
        RuleJsonDTO ruleCdcDTOAfter = JsonUtils.parseObject(mysqlCdcDTO.getAfter(), RuleJsonDTO.class);
        if (Objects.isNull(ruleCdcDTOAfter)) {
            ruleCdcDTOAfter = new RuleJsonDTO();
        }
        Long ruleCodeAfter = ruleCdcDTOAfter.getRuleCode();
        RuleInfoDTO ruleInfoDTOAfter = JsonUtils.parseObject(ruleCdcDTOAfter.getRuleJson(), RuleInfoDTO.class);
        // 上下线规则运算机
        if (Envelope.Operation.CREATE.code().equals(op)) {
            // create: 只有发布上线规则的时候，才会出现创建操作，所以需要加载规则运算机
            loadProcessor(getRuntimeContext(), ruleCodeAfter, ruleInfoDTOAfter);
        } else if (Envelope.Operation.READ.code().equals(op)) {
            // read: 读操作意味着计算引擎是刚刚启动，我们需要从数据库中恢复加载之前已经上线的规则运算机
            loadProcessor(getRuntimeContext(), ruleCodeAfter, ruleInfoDTOAfter);
        } else if (Envelope.Operation.UPDATE.code().equals(op)) {
            // update: 因为上线规则时进行insert，下线规则时直接delete了，其他更新操作不会同步到rule_json表中，故忽略
            log.warn("规则运算机不支持在线热更新，请不要直接 update rule_json 表中的数据！");
        } else if (Envelope.Operation.DELETE.code().equals(op)) {
            // delete: 删除操作顾名思义，就是执行了下线操作，这个时候，我们只需要将规则运算机移除即可
            removeProcessor(ruleCodeBefore);
        }
        log.info("当前规则运算机数量: {}, 规则编号列表: {}", ruleProcessorPool.size(), ruleProcessorPool.keySet());
    }

    /**
     * 移除规则运算机
     */
    private void removeProcessor(Long ruleCode) throws Exception {
        if (Objects.isNull(ruleCode)) {
            log.warn("移除规则运算机失败，传入的规则编号不能为空！");
            return;
        }
        Processor processor = ruleProcessorPool.get(ruleCode);
        if (Objects.isNull(processor)) {
            log.warn("规则运算机不存在，无需移除，规则编号为: {}", ruleCode);
            return;
        }
        ruleProcessorPool.remove(ruleCode);
        ruleInfoPool.remove(ruleCode);
        log.info("下线一个规则运算机，规则编号为: {}", ruleCode);
    }

    /**
     * 加载规则运算机
     */
    private void loadProcessor(RuntimeContext runtimeContext, Long ruleCode, RuleInfoDTO ruleInfoDTO) throws Exception {
        if (Objects.isNull(ruleCode)) {
            log.warn("上线规则运算机失败，传入的规则编号不能为空！");
            return;
        }
        if (Objects.isNull(ruleInfoDTO)) {
            log.warn("上线规则运算机失败，传入的规则信息不能空！");
            return;
        }
        if (ruleProcessorPool.containsKey(ruleCode)) {
            return;
        }
        // 构建规则运算机
        Processor processor = buildProcessor(runtimeContext, null, ruleInfoDTO);
        ruleProcessorPool.put(ruleCode, processor);
        ruleInfoPool.put(ruleCode, ruleInfoDTO);
        log.info("上线一个规则运算机，规则编号为: {}", ruleCode);
    }

    /**
     * 加载规则运算机
     */
    private void loadProcessor(KeyedStateStore keyedStateStore, Long ruleCode, RuleInfoDTO ruleInfoDTO) throws Exception {
        if (Objects.isNull(ruleCode)) {
            log.warn("恢复规则运算机失败，传入的规则编号不能为空！");
            return;
        }
        if (Objects.isNull(ruleInfoDTO)) {
            log.warn("恢复规则运算机失败，传入的规则信息不能空！");
            return;
        }
        if (ruleProcessorPool.containsKey(ruleCode)) {
            return;
        }
        // 构建规则运算机
        Processor processor = buildProcessor(null, keyedStateStore, ruleInfoDTO);
        ruleProcessorPool.put(ruleCode, processor);
        ruleInfoPool.put(ruleCode, ruleInfoDTO);
        log.info("恢复了一个规则运算机，规则编号为: {}", ruleCode);
    }

    /**
     * 由于每个 key 都独立维护自己的计时器状态，若两个不同的 key 在相同的时间点触发了计时器，则 onTimer 方法会被调用两次。
     */
    @Override
    public void onTimer(long timestamp,
                        KeyedBroadcastProcessFunction<String, KafkaEventDTO, MysqlCdcDTO, ResultDTO>.OnTimerContext ctx,
                        Collector<ResultDTO> out) throws Exception {
        // 获取当前Key
        String currentKey = ctx.getCurrentKey();
        // 判断当前key所有运算机中是否有待处理的定时器
        boolean hasPendingTimers = false;
        // 数据遍历经过每个规则运算机
        for (Map.Entry<Long, Processor> stringProcessorEntry : ruleProcessorPool.entrySet()) {
            Processor processor = stringProcessorEntry.getValue();
            // 调用定时器
            boolean hasActiveEvents = processor.onTimer(currentKey, timestamp, out);
            if (hasActiveEvents) {
                hasPendingTimers = true;
            }
        }
        // 如果运算机中有待处理的定时器，则注册下一次flink定时器。
        if (hasPendingTimers) {
            // 注册下一次输出累积值的Timer。该timestamp就是窗口结束时刻，下一个窗口可以直接加60s。
            long nextTimerTime = timestamp + TimeUnit.MINUTES.toMillis(1);
            ctx.timerService().registerProcessingTimeTimer(nextTimerTime);
        }
    }

    /**
     * 构造运算机对象
     */
    private Processor buildProcessor(RuntimeContext runtimeContext, KeyedStateStore keyedStateStore, RuleInfoDTO ruleInfoDTO) throws Exception {
        String ruleModelGroovyCode = ruleInfoDTO.getModelGroovy();
        if (StringUtils.isNullOrWhitespaceOnly(ruleModelGroovyCode)) {
            throw new BusinessException("运算机模型代码 ruleModelGroovyCode 必须非空");
        }
        // TODO: 优化，使用缓存池，避免每次都创建新的ClassLoader
        Class<?> aClass = groovyClassLoader.parseClass(ruleModelGroovyCode);
        Processor processor = (Processor) aClass.newInstance();
        if (Objects.nonNull(runtimeContext)) {
            processor.init(runtimeContext, null, ruleInfoDTO);
        } else {
            processor.init(null, keyedStateStore, ruleInfoDTO);
        }
        return processor;
    }

    // mock运算机对象
//    private Processor mockProcessor(RuntimeContext runtimeContext, KeyedStateStore keyedStateStore, RuleInfoDTO ruleInfoDTO) throws Exception {
//        Processor processor = new ProcessorOne();
//        if (Objects.nonNull(runtimeContext)) {
//            processor.init(runtimeContext, null, ruleInfoDTO);
//        } else {
//            processor.init(null, keyedStateStore, ruleInfoDTO);
//        }
//        return processor;
//    }

    /**
     * checkpoint时调用的方法
     */
    @Override
    public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
        List<RuleInfoDTO> ruleInfoDTOList = new ArrayList<>(ruleInfoPool.values());
        restoreRuleInfoListState.update(ruleInfoDTOList);
    }

    /**
     * 恢复状态时调用的方法
     */
    @Override
    public void initializeState(FunctionInitializationContext functionInitializationContext) throws Exception {
        if (groovyClassLoader == null) {
            groovyClassLoader = new GroovyClassLoader();
        }
        // 获取用于存储规则信息的 UnionList 算子状态
        restoreRuleInfoListState = functionInitializationContext.getOperatorStateStore()
                .getUnionListState(CommonStateDesc.RESTORE_RULE_INFO_LIST_STATE_DESC);
        // 遍历 UnionList 算子状态，恢复构建规则运算机，并进行初始化
        for (RuleInfoDTO ruleInfoDTO : restoreRuleInfoListState.get()) {
            loadProcessor(functionInitializationContext.getKeyedStateStore(), ruleInfoDTO.getRuleCode(), ruleInfoDTO);
        }
        log.info("恢复后的规则运算机数量: {}, 规则编号列表: {}", ruleProcessorPool.size(), ruleProcessorPool.keySet());
    }
}