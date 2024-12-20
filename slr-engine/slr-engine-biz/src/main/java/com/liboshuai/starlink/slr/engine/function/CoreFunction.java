package com.liboshuai.starlink.slr.engine.function;

import com.liboshuai.starlink.slr.engine.api.dto.*;
import com.liboshuai.starlink.slr.engine.api.enums.RuleStatusEnum;
import com.liboshuai.starlink.slr.engine.common.ParameterConstants;
import com.liboshuai.starlink.slr.engine.dto.RuleCdcDTO;
import com.liboshuai.starlink.slr.engine.exception.BusinessException;
import com.liboshuai.starlink.slr.engine.processor.Processor;
import com.liboshuai.starlink.slr.engine.processor.impl.ProcessorOne;
import com.liboshuai.starlink.slr.engine.utils.jdbc.JdbcUtil;
import com.liboshuai.starlink.slr.engine.utils.parameter.ParameterUtil;
import com.liboshuai.starlink.slr.engine.utils.string.JsonUtil;
import groovy.lang.GroovyClassLoader;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.util.Collector;
import org.apache.flink.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.liboshuai.starlink.slr.engine.common.StateDescContainer.*;

/**
 * 计算引擎核心function
 */
@Slf4j
public class CoreFunction extends KeyedBroadcastProcessFunction<String, EventKafkaDTO, RuleCdcDTO, String> {

    private static final long serialVersionUID = -5913085790319815064L;

    /**
     * 运算机map：key-规则编号，value-运算机
     */
    private Map<String, Processor> processorByRuleCodeMap;

    /**
     * groovy加载器
     */
    private GroovyClassLoader groovyClassLoader;

    /**
     * 最近15分钟时间事件数据缓存
     */
    private ListState<EventKafkaDTO> recentEventListState;

    /**
     * 旧规则列表
     */
    private MapState<String, Object> oldRuleListState;

    /**
     * 在线规则数量
     */
    private AtomicLong onlineRuleCount;

    /**
     * 银行数据
     */
    private Map<String, String> bankMapState;

    /**
     * 注意千万不要在open方法中对状态进行赋值操作，因为在processElement等方法中并不能获取到
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        processorByRuleCodeMap = new ConcurrentHashMap<>();
        groovyClassLoader = new GroovyClassLoader();
        RECENT_EVENT_LIST_STATE_DESC
                .enableTimeToLive(StateTtlConfig.newBuilder(Time.minutes(15)).neverReturnExpired().build());
        recentEventListState = getRuntimeContext().getListState(RECENT_EVENT_LIST_STATE_DESC);
        oldRuleListState = getRuntimeContext().getMapState(OLD_RULE_MAP_STATE_DESC);
        // 查询在线规则数量
        onlineRuleCount = queryOnlineRuleCount();
        // 查询银行数据
        bankMapState = queryBank();
    }

    @Override
    public void processElement(EventKafkaDTO eventKafkaDTO,
                               KeyedBroadcastProcessFunction<String, EventKafkaDTO, RuleCdcDTO, String>.ReadOnlyContext ctx,
                               Collector<String> out) throws Exception {
        long processTime = ctx.currentProcessingTime();
        // 等待所有运算机初始化完成
        waitForInitAllProcessor();
        // 将事件放入缓存列表中，并记录存放的时间
        eventKafkaDTO.setTimestamp(processTime);
        recentEventListState.add(eventKafkaDTO);
        // 从广播流中获取规则信息
        ReadOnlyBroadcastState<String, RuleInfoDTO> broadcastState = ctx.getBroadcastState(BROADCAST_RULE_MAP_STATE_DESC);
        // 数据遍历经过每个规则运算机
        for (Map.Entry<String, Processor> stringProcessorEntry : processorByRuleCodeMap.entrySet()) {
            String ruleCode = stringProcessorEntry.getKey();
            Processor processor = stringProcessorEntry.getValue();
            if (!oldRuleListState.contains(ruleCode)) {
                // 新规则需要先将缓存的最近历史事件数据处理一遍
                for (EventKafkaDTO historyEventKafkaDTO : recentEventListState.get()) {
                    processor.processElement(processTime, historyEventKafkaDTO, broadcastState.get(ruleCode), out);
                }
                oldRuleListState.put(ruleCode, null);
            } else {
                // 否则直接处理当前一条事件数据即可
                processor.processElement(processTime, eventKafkaDTO, broadcastState.get(ruleCode), out);
            }
        }
        // 注册定时器（窗口大小1分钟）
        // long fireTime = Long.parseLong(timestamp) - Long.parseLong(timestamp) % 60000 + 60000; （简化写法）
        long fireTime = getWindowStartWithOffset(processTime, 0, 60 * 1000) + 60 * 1000;
        ctx.timerService().registerProcessingTimeTimer(fireTime);
    }

    private static Set<Integer> getOnlineStatuses() {
        Set<Integer> onlineStatuses = new HashSet<>();
        onlineStatuses.add(RuleStatusEnum.ONLINE.getCode());
        onlineStatuses.add(RuleStatusEnum.PENDING_OFFLINE_REVIEW.getCode());
        return onlineStatuses;
    }

    private static Set<String> getValidOperations() {
        Set<String> validOperations = new HashSet<>();
        validOperations.add(Envelope.Operation.READ.code());
        validOperations.add(Envelope.Operation.CREATE.code());
        validOperations.add(Envelope.Operation.UPDATE.code());
        return validOperations;
    }

    @Override
    public void processBroadcastElement(RuleCdcDTO ruleCdcDTO,
                                        KeyedBroadcastProcessFunction<String, EventKafkaDTO, RuleCdcDTO, String>.Context ctx,
                                        Collector<String> out) throws Exception {
        if (ruleCdcDTO == null) {
            throw new BusinessException("Mysql Cdc 广播流 ruleCdcDTO 必须非空");
        }
        // cdc数据更新
        String op = ruleCdcDTO.getOp();
        RuleJsonDTO ruleCdcDTOAfter = ruleCdcDTO.getAfter();
        String ruleCode = ruleCdcDTOAfter.getRuleCode();
        String ruleJson = ruleCdcDTOAfter.getRuleJson();
        RuleInfoDTO ruleInfoDTO = JsonUtil.parseObject(ruleJson, RuleInfoDTO.class);
        if (Objects.isNull(ruleInfoDTO)) {
            throw new BusinessException("Mysql Cdc 广播流 ruleInfoDTO 必须非空");
        }
        BroadcastState<String, RuleInfoDTO> broadcastState = ctx.getBroadcastState(BROADCAST_RULE_MAP_STATE_DESC);
        // 上线规则时cdc操作类型集合
        Set<String> validOperations = getValidOperations();
        // 已上线规则状态集合
        Set<Integer> onlineStatuses = getOnlineStatuses();
        // 上下线规则
        if (Envelope.Operation.READ.code().equals(op) && onlineStatuses.contains(ruleInfoDTO.getStatus())) {
            // read 操作时，将'已上线'、'下线待审核' 的规则均进行加载
            Processor processor = buildProcessor(getRuntimeContext(), ruleInfoDTO);
            processorByRuleCodeMap.put(ruleCode, processor);
            broadcastState.put(ruleCode, ruleInfoDTO);
            log.warn("上线一个运算机，规则编号为: {}", ruleCode);
        } else if (Envelope.Operation.CREATE.code().equals(op) && onlineStatuses.contains(ruleInfoDTO.getStatus())) {

        }

        if (validOperations.contains(op) && onlineStatuses.contains(ruleInfoDTO.getStatus())) {
            // 在读取、创建、更新，且状态为上线、下线待审核时，则上线一个运算机
            // Processor processor = buildProcessor(getRuntimeContext(), ruleInfoDTO);
            Processor processor = mockProcessor(getRuntimeContext(), ruleInfoDTO);
            processorByRuleCodeMap.put(ruleCode, processor);
            broadcastState.put(ruleCode, ruleInfoDTO);
            log.warn("上线或更新一个运算机，规则编号为: {}", ruleCode);
        } else if (Envelope.Operation.UPDATE.code().equals(op)
                && RuleStatusEnum.OFFLINE.getCode() == ruleInfoDTO.getStatus()) {
            // 在更新，且状态为下线时，则下线一个运算机
            if (processorByRuleCodeMap.containsKey(ruleCode)) {
                processorByRuleCodeMap.remove(ruleCode);
                broadcastState.remove(ruleCode);
                log.warn("下线一个运算机，规则编号为: {}", ruleCode);
            }
        }
        log.warn("当前规则运算机数量: {}", processorByRuleCodeMap.size());
    }

    /**
     * 由于每个 key 都独立维护自己的计时器状态，若两个不同的 key 在相同的时间点触发了计时器，则 onTimer 方法会被调用两次。
     */
    @Override
    public void onTimer(long timestamp,
                        KeyedBroadcastProcessFunction<String, EventKafkaDTO, RuleCdcDTO, String>.OnTimerContext ctx,
                        Collector<String> out) throws Exception {
        // 从广播流中获取规则信息
        ReadOnlyBroadcastState<String, RuleInfoDTO> broadcastState = ctx.getBroadcastState(BROADCAST_RULE_MAP_STATE_DESC);
        // 数据遍历经过每个规则运算机
        for (Map.Entry<String, Processor> stringProcessorEntry : processorByRuleCodeMap.entrySet()) {
            String ruleCode = stringProcessorEntry.getKey();
            Processor processor = stringProcessorEntry.getValue();
            // 调用定时器
            processor.onTimer(timestamp, broadcastState.get(ruleCode), out);
        }
    }

    /**
     * 构造运算机对象
     */
    private Processor buildProcessor(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) throws Exception {
        String ruleModelGroovyCode = ruleInfoDTO.getRuleModelGroovyCode();
        if (StringUtils.isNullOrWhitespaceOnly(ruleModelGroovyCode)) {
            throw new BusinessException("运算机模型代码 ruleModelGroovyCode 必须非空");
        }
        Class aClass = groovyClassLoader.parseClass(ruleModelGroovyCode);
        Processor processor = (Processor) aClass.newInstance();
        processor.init(runtimeContext, ruleInfoDTO);
        return processor;
    }

    /**
     * mock运算机对象
     */
    private Processor mockProcessor(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) throws Exception {
        Processor processor = new ProcessorOne();
        processor.init(runtimeContext, ruleInfoDTO);
        return processor;
    }

    /**
     * 查询上线的规则数量
     */
    private AtomicLong queryOnlineRuleCount() {
        // 获取规则表名
        String tableName = ParameterUtil.getParameters().get(ParameterConstants.MYSQL_TABLE_RULE_COUNT);
        // 查询规则数据
        String sql = "select online_count from " + tableName + " where deleted = 0";
        RuleOnlineCountDTO ruleOnlineCountDTO = JdbcUtil.queryOne(
                sql, new JdbcUtil.BeanPropertyRowMapper<>(RuleOnlineCountDTO.class)
        );
        if (Objects.isNull(ruleOnlineCountDTO)) {
            throw new BusinessException("Mysql Jdbc 查询上线的规则数量为空！");
        }
        log.warn("Mysql Jdbc 查询上线的规则数量: {}", ruleOnlineCountDTO.getOnlineCount());
        return new AtomicLong(ruleOnlineCountDTO.getOnlineCount());
    }

    /**
     * 查询银行数据
     */
    private Map<String, String> queryBank() throws Exception {
        Map<String, String> bankMap = new HashMap<>();
        // 获取规则表名
        String tableName = ParameterUtil.getParameters().get(ParameterConstants.MYSQL_TABLE_BANK);
        // 查询规则数据
        String sql = "select bank, name from " + tableName + " where is_deleted = 0";
        List<BankDTO> bankDTOList = JdbcUtil.queryList(sql, new JdbcUtil.BeanPropertyRowMapper<>(BankDTO.class));
        if (CollectionUtil.isNullOrEmpty(bankDTOList)) {
            log.warn("Mysql Jdbc 预加载的银行对象集合bankDTOList为空！");
            return new HashMap<>();
        }
        log.warn("Mysql Jdbc 预加载的银行对象集合bankDTOList: {}", JsonUtil.toJsonString(bankDTOList));
        for (BankDTO bankDTO : bankDTOList) {
            bankMap.put(bankDTO.getBank(), bankDTO.getName());
        }
        return bankMap;
    }

    /**
     * 等待所有运算机初始化完成
     */
    private void waitForInitAllProcessor() throws InterruptedException {
        long currentOnlineRuleCount = getCurrentOnlineRuleCount();
        while (onlineRuleCount.get() != currentOnlineRuleCount) {
            log.warn("等待所有运算机初始化完成: MySQL库中上线规则数量={}, 运算机池中上线的规则数量={}", onlineRuleCount, currentOnlineRuleCount);
            TimeUnit.SECONDS.sleep(1);
            onlineRuleCount = queryOnlineRuleCount();
            currentOnlineRuleCount = getCurrentOnlineRuleCount();
        }
    }

    /**
     * 获取当前在线规则的数量
     */
    private long getCurrentOnlineRuleCount() {
        Set<String> ruleCodeCount = processorByRuleCodeMap.keySet();
        return ruleCodeCount.size();
    }

    private long getWindowStartWithOffset(long timestamp, long offset, long windowSize) {
        final long remainder = (timestamp - offset) % windowSize;
        // handle both positive and negative cases
        if (remainder < 0) {
            return timestamp - (remainder + windowSize);
        } else {
            return timestamp - remainder;
        }
    }
}