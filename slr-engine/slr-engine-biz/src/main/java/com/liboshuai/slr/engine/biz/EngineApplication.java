package com.liboshuai.slr.engine.biz;


import com.liboshuai.slr.engine.api.dto.DorisEventDTO;
import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.FlinkResultDTO;
import com.liboshuai.slr.engine.api.dto.MysqlCdcDTO;
import com.liboshuai.slr.engine.biz.constants.ParameterConstants;
import com.liboshuai.slr.engine.biz.convert.EventConvert;
import com.liboshuai.slr.engine.biz.framework.connector.FlinkDorisConnector;
import com.liboshuai.slr.engine.biz.framework.connector.FlinkKafkaConnector;
import com.liboshuai.slr.engine.biz.framework.connector.FlinkMysqlCdcConnector;
import com.liboshuai.slr.engine.biz.framework.state.CommonStateDesc;
import com.liboshuai.slr.engine.biz.function.*;
import com.liboshuai.slr.engine.biz.util.ParameterUtil;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
public class EngineApplication {

    public static void main(String[] args) throws Exception {
        //流式计算上下文环境
        final StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();
        //ParameterTool 注册为 global
        ParameterTool parameterTool = ParameterUtil.getParameters(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        // 配置上下文环境
        ParameterUtil.envWithConfig(env, parameterTool);

        // 获取kafka源topic分区数
        int kafkaPartition = parameterTool.getInt(ParameterConstants.KAFKA_SOURCE_TOPIC_PARTITION);
        // 获取规则配置数据流
        DataStream<MysqlCdcDTO> ruleDS = FlinkMysqlCdcConnector.read(env, parameterTool);
        // 获取旧状态清理流
        SingleOutputStreamOperator<FlinkEventDTO> clearFlinkEventDtoSO = AsyncDataStream.unorderedWait(
                        ruleDS, new DorisAsyncFunction(parameterTool), 1, TimeUnit.MINUTES, 100
                ).setParallelism(kafkaPartition)
                .returns(Types.POJO(FlinkEventDTO.class)).uid("async-doris");
        // 获取规则广播流
        BroadcastStream<MysqlCdcDTO> broadcastStream = ruleDS.broadcast(CommonStateDesc.BROADCAST_RULE_MAP_STATE_DESC);
        // 获取业务数据流
        SingleOutputStreamOperator<FlinkEventDTO> flinkEventDtoDS = FlinkKafkaConnector.read(env, parameterTool)
                // 转换string为FlinkEventDto对象
                .map(new Json2FlinkEventDtoMapFunction())
                .setParallelism(kafkaPartition).returns(Types.POJO(FlinkEventDTO.class)).uid("flinkEventDTO-process")
                // 过滤掉非法的事件
                .filter(new FlinkEventFilterFunction())
                .setParallelism(kafkaPartition).returns(Types.POJO(FlinkEventDTO.class)).uid("flinkEventDTO-filter")
                // 补充事件时间
                .process(new FlinkEventProcessFunction()).returns(Types.POJO(FlinkEventDTO.class)).uid("flinkEventDTO-process");
        // 实时动态规则引擎
        SingleOutputStreamOperator<FlinkResultDTO> resultDtoStreamOperator = flinkEventDtoDS
                // 使用处理时间
                .assignTimestampsAndWatermarks(WatermarkStrategy.noWatermarks())
                .setParallelism(kafkaPartition).returns(Types.POJO(FlinkEventDTO.class)).uid("register-watermark")
                // 合并数据清洗流
                .union(clearFlinkEventDtoSO)
                // keyBy分组
                .keyBy(new FlinkEventKeyBy())
                // 连接规则配置流
                .connect(broadcastStream)
                // 核心处理逻辑
                .process(new CoreFunction())
                .returns(Types.POJO(FlinkResultDTO.class)).uid("core-function");
        // 将事件数据写入 doris
        writeEventToDoris(flinkEventDtoDS, parameterTool);
        // 将规则状态写入doris，以便规则下线清除状态使用
        writeStateToDoris(resultDtoStreamOperator, parameterTool);
        // 将告警信息写入 kafka
        writeAlertToKafka(resultDtoStreamOperator, parameterTool);
        env.execute();
    }

    /**
     * 将告警信息写入kafka
     */
    private static void writeAlertToKafka(SingleOutputStreamOperator<FlinkResultDTO> resultDtoStreamOperator, ParameterTool parameterTool) {
        SingleOutputStreamOperator<String> warnMessageStreamOperator = resultDtoStreamOperator
                // 非法数据过滤
                .filter(resultDTO -> Objects.nonNull(resultDTO.getAlertDTO()))
                .returns(Types.POJO(FlinkResultDTO.class)).uid("alert-filter")
                // 实体类转json
                .map(resultDTO -> JsonUtils.toJsonString(resultDTO.getAlertDTO()))
                .returns(Types.STRING).uid("alert-map");
        FlinkKafkaConnector.writer(warnMessageStreamOperator, parameterTool);
    }

    /**
     * 将规则状态历史写入doris
     */
    private static void writeStateToDoris(SingleOutputStreamOperator<FlinkResultDTO> resultDtoStreamOperator, ParameterTool parameterTool) {
        SingleOutputStreamOperator<String> streamOperator = resultDtoStreamOperator
                // 非法数据过滤
                .filter(resultDTO -> Objects.nonNull(resultDTO.getStateDTO()))
                .returns(Types.POJO(FlinkResultDTO.class)).uid("state-filter")
                // 实体类转json
                .map(resultDTO -> JsonUtils.toJsonStringWithUpperSnakeCaseKeys(resultDTO.getStateDTO()))
                .returns(Types.STRING).uid("state-map");
        String database = parameterTool.get(ParameterConstants.DORIS_DATABASE);
        String tableName = parameterTool.get(ParameterConstants.DORIS_TABLE_STATE);
        FlinkDorisConnector.writer(database + DefaultConstants.POINT + tableName, streamOperator, parameterTool);
    }

    /**
     * 将事件数据写入 doris
     */
    private static void writeEventToDoris(SingleOutputStreamOperator<FlinkEventDTO> flinkEventDtoDS, ParameterTool parameterTool) {
        SingleOutputStreamOperator<String> streamOperator = flinkEventDtoDS
                // 实体类转json
                .map(flinkEventDTO -> {
                    DorisEventDTO dorisEventDTO = EventConvert.INSTANCE.flinkDto2DorisDto(flinkEventDTO);
                    // 转为大写下划线，适配doris表结构字段
                    return JsonUtils.toJsonStringWithUpperSnakeCaseKeys(dorisEventDTO);
                }).returns(Types.STRING).uid("toDoris-process");
        String database = parameterTool.get(ParameterConstants.DORIS_DATABASE);
        String tableName = parameterTool.get(ParameterConstants.DORIS_TABLE_EVENT);
        FlinkDorisConnector.writer(database + DefaultConstants.POINT + tableName, streamOperator, parameterTool);
    }

}
