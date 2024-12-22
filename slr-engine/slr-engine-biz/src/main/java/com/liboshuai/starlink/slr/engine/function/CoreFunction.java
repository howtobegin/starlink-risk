package com.liboshuai.starlink.slr.engine.function;

import com.liboshuai.starlink.slr.engine.api.dto.*;
import com.liboshuai.starlink.slr.engine.constants.ParameterConstants;
import com.liboshuai.starlink.slr.engine.dto.RuleCdcDTO;
import com.liboshuai.starlink.slr.engine.exception.BusinessException;
import com.liboshuai.starlink.slr.engine.processor.Processor;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.liboshuai.starlink.slr.engine.common.StateDescContainer.*;

/**
 * 计算引擎核心function
 */
@Slf4j
public class CoreFunction extends KeyedBroadcastProcessFunction<String, KafkaEventDTO, RuleCdcDTO, String> {

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
     * 最近15分钟时间事件数据缓存
     */
    private ListState<KafkaEventDTO> recentEventListState;

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
     * （key为银行code，value为银行名称）
     */
    private Map<String, String> bankMap;

    /**
     * 注意千万不要在open方法中对状态进行赋值操作，因为在processElement等方法中并不能获取到
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        ruleProcessorPool = new ConcurrentHashMap<>();
        groovyClassLoader = new GroovyClassLoader();
        RECENT_EVENT_LIST_STATE_DESC
                .enableTimeToLive(StateTtlConfig.newBuilder(Time.minutes(15)).neverReturnExpired().build());
        recentEventListState = getRuntimeContext().getListState(RECENT_EVENT_LIST_STATE_DESC);
        oldRuleListState = getRuntimeContext().getMapState(OLD_RULE_MAP_STATE_DESC);
        // 查询在线规则数量
        onlineRuleCount = queryOnlineRuleCount();
        // 查询银行数据
        bankMap = queryBank();
    }

    @Override
    public void processElement(KafkaEventDTO kafkaEventDTO,
                               KeyedBroadcastProcessFunction<String, KafkaEventDTO, RuleCdcDTO, String>.ReadOnlyContext ctx,
                               Collector<String> out) throws Exception {
        long processTime = ctx.currentProcessingTime();
        // 等待所有运算机初始化完成
        waitForInitAllProcessor();
        // 将事件放入缓存列表中，并记录存放的时间
        kafkaEventDTO.setTimestamp(processTime);
        recentEventListState.add(kafkaEventDTO);
        // 从广播流中获取规则信息
        ReadOnlyBroadcastState<String, RuleInfoDTO> broadcastState = ctx.getBroadcastState(BROADCAST_RULE_MAP_STATE_DESC);
        // 数据遍历经过每个规则运算机
        for (Map.Entry<String, Processor> stringProcessorEntry : ruleProcessorPool.entrySet()) {
            String ruleCode = stringProcessorEntry.getKey();
            Processor processor = stringProcessorEntry.getValue();
            if (!oldRuleListState.contains(ruleCode)) {
                // 新规则需要先将缓存的最近历史事件数据处理一遍
                for (KafkaEventDTO historyKafkaEventDTO : recentEventListState.get()) {
                    processor.processElement(processTime, broadcastState.get(ruleCode), historyKafkaEventDTO, out);
                }
                oldRuleListState.put(ruleCode, null);
            } else {
                // 否则直接处理当前一条事件数据即可
                processor.processElement(processTime, broadcastState.get(ruleCode), kafkaEventDTO, out);
            }
        }
        // 注册定时器（窗口大小1分钟）
        // long fireTime = Long.parseLong(timestamp) - Long.parseLong(timestamp) % 60000 + 60000; （简化写法）
        long fireTime = getWindowStartWithOffset(processTime, 0, 60 * 1000) + 60 * 1000;
        ctx.timerService().registerProcessingTimeTimer(fireTime);
    }

    @Override
    public void processBroadcastElement(RuleCdcDTO ruleCdcDTO,
                                        KeyedBroadcastProcessFunction<String, KafkaEventDTO, RuleCdcDTO, String>.Context ctx,
                                        Collector<String> out) throws Exception {
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
        RuleInfoDTO ruleInfoDTOAfter = JsonUtil.parseObject(ruleJsonAfter, RuleInfoDTO.class);
        // 获取广播流数据
        BroadcastState<String, RuleInfoDTO> broadcastState = ctx.getBroadcastState(BROADCAST_RULE_MAP_STATE_DESC);
        // 上下线规则运算机
        if (Envelope.Operation.CREATE.code().equals(op)) {
            // create: 只有发布上线规则的时候，才会出现创建操作，所以需要加载规则运算机
            loadProcessor(ruleCodeAfter, broadcastState, ruleInfoDTOAfter);
        } else if (Envelope.Operation.READ.code().equals(op)) {
            // read: 读操作意味着计算引擎是刚刚启动，我们需要从数据库中恢复加载之前已经上线的规则运算机
            loadProcessor(ruleCodeAfter, broadcastState, ruleInfoDTOAfter);
        }else if (Envelope.Operation.UPDATE.code().equals(op)) {
            // update: 因为上线规则时进行insert，下线规则时直接delete了，其他更新操作不会同步到rule_json表中，故忽略
            log.warn("规则运算机不支持在线热更新，请不要直接 update rule_json 表中的数据！");
        } else if (Envelope.Operation.DELETE.code().equals(op)) {
            // delete: 删除操作顾名思义，就是执行了下线操作，这个时候，我们只需要将规则运算机移除即可
            removeProcessor(ruleCodeBefore, broadcastState);
        }
        log.warn("当前规则运算机数量: {}", ruleProcessorPool.size());
        log.warn("当前规则运算机规则编号列表: {}", ruleProcessorPool.keySet());
    }

    /**
     * 移除规则运算机
     */
    private void removeProcessor(String ruleCode, BroadcastState<String, RuleInfoDTO> broadcastState) throws Exception {
        if (!ruleProcessorPool.containsKey(ruleCode)) {
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
        Processor processor = buildProcessor(getRuntimeContext(), ruleInfoDTO);
        ruleProcessorPool.put(ruleCode, processor);
        broadcastState.put(ruleCode, ruleInfoDTO);
        log.warn("上线一个规则运算机，规则编号为: {}", ruleCode);
    }

    /**
     * 由于每个 key 都独立维护自己的计时器状态，若两个不同的 key 在相同的时间点触发了计时器，则 onTimer 方法会被调用两次。
     */
    @Override
    public void onTimer(long timestamp,
                        KeyedBroadcastProcessFunction<String, KafkaEventDTO, RuleCdcDTO, String>.OnTimerContext ctx,
                        Collector<String> out) throws Exception {
        // 从广播流中获取规则信息
        ReadOnlyBroadcastState<String, RuleInfoDTO> broadcastState = ctx.getBroadcastState(BROADCAST_RULE_MAP_STATE_DESC);
        // 数据遍历经过每个规则运算机
        for (Map.Entry<String, Processor> stringProcessorEntry : ruleProcessorPool.entrySet()) {
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
        ModelInfoDTO modelInfoDTO = ruleInfoDTO.getModelInfoDTO();
        if (Objects.isNull(modelInfoDTO)) {
            throw new BusinessException("运算机模型信息 modelInfoDTO 必须非空");
        }
        String ruleModelGroovyCode = modelInfoDTO.getGroovy();
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
//    private Processor mockProcessor(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) throws Exception {
//        Processor processor = new ProcessorOne();
//        processor.init(runtimeContext, ruleInfoDTO);
//        return processor;
//    }

    /**
     * 查询上线的规则数量
     */
    private AtomicLong queryOnlineRuleCount() {
        // 获取规则表名
        String tableName = ParameterUtil.getParameters().get(ParameterConstants.MYSQL_TABLE_RULE_JSON);
        // 查询规则数据
        String sql = String.format("select count(*) from %s", tableName);
        Long ruleOnlineCount = JdbcUtil.queryForObject(
                sql, new JdbcUtil.SingleColumnRowMapper<>(Long.class), null);
        if (Objects.isNull(ruleOnlineCount)) {
            throw new BusinessException("Mysql Jdbc 查询上线的规则数量为 null！");
        }
        log.warn("Mysql Jdbc 查询上线的规则数量: {}", ruleOnlineCount);
        return new AtomicLong(ruleOnlineCount);
    }

    /**
     * 查询银行数据
     */
    private Map<String, String> queryBank() {
        Map<String, String> bankMap = new ConcurrentHashMap<>();
        // 获取规则表名
        String tableName = ParameterUtil.getParameters().get(ParameterConstants.MYSQL_TABLE_BANK);
        // 查询规则数据
        String sql = String.format("select bank, name from %s", tableName);
        List<BankDTO> bankDTOList = JdbcUtil.queryForList(sql, new JdbcUtil.BeanPropertyRowMapper<>(BankDTO.class), null);
        if (CollectionUtil.isNullOrEmpty(bankDTOList)) {
            log.warn("Mysql Jdbc 预加载的银行对象集合bankDTOList为空！");
            return new ConcurrentHashMap<>();
        }
        for (BankDTO bankDTO : bankDTOList) {
            bankMap.put(bankDTO.getBank(), bankDTO.getName());
        }
        log.warn("Mysql Jdbc 预加载的银行对象集合 bankMap: {}", bankMap);
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
        Set<String> ruleCodeCount = ruleProcessorPool.keySet();
        return ruleCodeCount.size();
    }

    /**
     * 获取窗口开始时间
     */
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