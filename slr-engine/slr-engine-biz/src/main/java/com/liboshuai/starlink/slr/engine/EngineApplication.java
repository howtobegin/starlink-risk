package com.liboshuai.starlink.slr.engine;


import com.liboshuai.starlink.slr.engine.api.constants.GlobalConstants;
import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.starlink.slr.engine.common.FlinkDorisConnector;
import com.liboshuai.starlink.slr.engine.common.FlinkKafkaConnector;
import com.liboshuai.starlink.slr.engine.common.FlinkMysqlConnector;
import com.liboshuai.starlink.slr.engine.common.StateDescContainer;
import com.liboshuai.starlink.slr.engine.dto.RuleCdcDTO;
import com.liboshuai.starlink.slr.engine.function.CoreFunction;
import com.liboshuai.starlink.slr.engine.function.KafkaEventFilterFunction;
import com.liboshuai.starlink.slr.engine.function.KafkaEventMapFunction;
import com.liboshuai.starlink.slr.engine.utils.JsonUtil;
import com.liboshuai.starlink.slr.engine.utils.ParameterUtil;
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
        // 获取业务数据流
        SingleOutputStreamOperator<KafkaEventDTO> kafkaEventDTOOperator = FlinkKafkaConnector.read(env, parameterTool)
                // 转换string为eventKafkaDTO对象，并设置处理时间
                .map(new KafkaEventMapFunction())
                // 过滤掉非法的事件
                .filter(new KafkaEventFilterFunction());
        // 将kafka中的事件数据同步往 doris 中留存一份
        SingleOutputStreamOperator<String> toDorisStreamOperator = kafkaEventDTOOperator
                .map(JsonUtil::toJsonStringWithUpperSnakeCaseKeys);
        FlinkDorisConnector.writer(toDorisStreamOperator, parameterTool);
        // 实时动态规则引擎
        SingleOutputStreamOperator<String> warnMessageStream = kafkaEventDTOOperator
                .assignTimestampsAndWatermarks(WatermarkStrategy.noWatermarks())// 使用处理时间
                .uid("register-watermark")
                .keyBy(eventKafkaDTO ->
                        eventKafkaDTO.getKeyCode() + GlobalConstants.FLINK_KEY_SEPARATOR + eventKafkaDTO.getKeyValue()
                )// keyBy分组
                .connect(broadcastStream) // 连接规则配置流
                .process(new CoreFunction())
                .uid("engine-core-function");
        // 将告警信息写入kafka
        FlinkKafkaConnector.writer(warnMessageStream, parameterTool);
        env.execute();
    }

}
