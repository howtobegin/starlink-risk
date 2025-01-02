package com.liboshuai.slr.module.engine;


import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.dto.RuleCdcDTO;
import com.liboshuai.slr.module.engine.framework.connector.FlinkDorisConnector;
import com.liboshuai.slr.module.engine.framework.connector.FlinkKafkaConnector;
import com.liboshuai.slr.module.engine.framework.connector.FlinkMysqlConnector;
import com.liboshuai.slr.module.engine.framework.state.StateDescContainer;
import com.liboshuai.slr.module.engine.function.*;
import com.liboshuai.slr.module.engine.utils.JsonUtil;
import com.liboshuai.slr.module.engine.utils.ParameterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


@Slf4j
public class EngineApplication {

    public static void main(String[] args) throws Exception {
        //流式计算上下文环境/
        Configuration configuration = new Configuration();
        configuration.setInteger("rest.port", 8989); // 设置本地 web 页面端口
        final StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment(configuration);
        //ParameterTool 注册为 global
        ParameterTool parameterTool = ParameterUtil.getParameters(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        // 配置上下文环境
        ParameterUtil.envWithConfig(env, parameterTool);

        // 获取规则配置数据流
        DataStream<RuleCdcDTO> ruleSource = FlinkMysqlConnector.read(env, parameterTool);
        // 获取规则广播流
        BroadcastStream<RuleCdcDTO> broadcastStream = ruleSource.broadcast(StateDescContainer.BROADCAST_RULE_MAP_STATE_DESC);
        // 获取自定义心跳数据流，用于触发定时器
        SingleOutputStreamOperator<KafkaEventDTO> heartbeatSource = env.addSource(new HeartbeatSource()).name("heartbeatSource");
        // 获取业务数据流
        SingleOutputStreamOperator<KafkaEventDTO> kafkaEventDTOOperator = FlinkKafkaConnector.read(env, parameterTool)
                // 转换string为eventKafkaDTO对象
                .map(jsonValue -> JsonUtil.parseObject(jsonValue, KafkaEventDTO.class)).uid("kafkaEventDTO-process-function")
                // 过滤掉非法的事件
                .filter(new KafkaEventFilterFunction()).uid("kafkaEventDTO-filter-function");
        // 将kafka中的事件数据同步往 doris 中留存一份
        SingleOutputStreamOperator<String> toDorisStreamOperator = kafkaEventDTOOperator
                // kafka事件数据结构转doris事件数据结构，并设置事件时间
                .process(new DorisEventProcessFunction()).uid("toDoris-process-function");
        FlinkDorisConnector.writer(toDorisStreamOperator, parameterTool);
        // 实时动态规则引擎
        SingleOutputStreamOperator<String> warnMessageStream = kafkaEventDTOOperator
                // 使用处理时间
                .assignTimestampsAndWatermarks(WatermarkStrategy.noWatermarks()).uid("register-watermark")
                .union(heartbeatSource) // 合并心跳数据流
                .keyBy(new KafkaEventKeyBy())// keyBy分组
                .connect(broadcastStream)// 连接规则配置流
                .process(new CoreFunction()).uid("core-function")
                .map(JsonUtil::toJsonString).uid("warnMessage-map-function");
        // 将告警信息写入kafka
        FlinkKafkaConnector.writer(warnMessageStream, parameterTool);
        env.execute();
    }

}
