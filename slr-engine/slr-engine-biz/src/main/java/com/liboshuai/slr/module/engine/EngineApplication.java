package com.liboshuai.slr.module.engine;


import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.module.engine.constants.ParameterConstants;
import com.liboshuai.slr.module.engine.convert.EventConvert;
import com.liboshuai.slr.module.engine.dto.DorisEventDTO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.dto.MysqlCdcDTO;
import com.liboshuai.slr.module.engine.dto.ResultDTO;
import com.liboshuai.slr.module.engine.framework.connector.FlinkDorisConnector;
import com.liboshuai.slr.module.engine.framework.connector.FlinkKafkaConnector;
import com.liboshuai.slr.module.engine.framework.connector.FlinkMysqlCdcConnector;
import com.liboshuai.slr.module.engine.framework.state.CommonStateDesc;
import com.liboshuai.slr.module.engine.function.CoreFunction;
import com.liboshuai.slr.module.engine.function.DorisAsyncFunction;
import com.liboshuai.slr.module.engine.function.KafkaEventFilterFunction;
import com.liboshuai.slr.module.engine.function.KafkaEventKeyBy;
import com.liboshuai.slr.module.engine.utils.ParameterUtil;
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

        // 获取规则配置数据流
        DataStream<MysqlCdcDTO> ruleDS = FlinkMysqlCdcConnector.read(env, parameterTool);
        // 获取旧状态清理流
        SingleOutputStreamOperator<KafkaEventDTO> clearKafkaEventDtoSO = AsyncDataStream.unorderedWait(
                ruleDS, new DorisAsyncFunction(parameterTool), 1, TimeUnit.MINUTES, 100
        ).returns(Types.POJO(KafkaEventDTO.class)).uid("async-doris");
        // 获取规则广播流
        BroadcastStream<MysqlCdcDTO> broadcastStream = ruleDS.broadcast(CommonStateDesc.BROADCAST_RULE_MAP_STATE_DESC);
        // 获取业务数据流
        SingleOutputStreamOperator<KafkaEventDTO> kafkaEventDtoDS = FlinkKafkaConnector.read(env, parameterTool)
                // 转换string为eventKafkaDTO对象
                .map(jsonValue -> JsonUtils.parseObject(jsonValue, KafkaEventDTO.class))
                .uid("kafkaEventDTO-process").returns(Types.POJO(KafkaEventDTO.class))
                // 过滤掉非法的事件
                .filter(new KafkaEventFilterFunction())
                .returns(Types.POJO(KafkaEventDTO.class))
                .uid("kafkaEventDTO-filter");
        // 实时动态规则引擎
        SingleOutputStreamOperator<ResultDTO> resultDtoStreamOperator = kafkaEventDtoDS
                // 使用处理时间
                .assignTimestampsAndWatermarks(WatermarkStrategy.noWatermarks())
                .returns(Types.POJO(KafkaEventDTO.class)).uid("register-watermark")
                // 合并数据清洗流
                .union(clearKafkaEventDtoSO)
                // keyBy分组
                .keyBy(new KafkaEventKeyBy())
                // 连接规则配置流
                .connect(broadcastStream)
                // 核心处理逻辑
                .process(new CoreFunction())
                .returns(Types.POJO(ResultDTO.class)).uid("core-function");
        // 将kafka中的事件数据同步往 doris 中留存一份
        writeKafkaEventToDoris(resultDtoStreamOperator, parameterTool);
        // 将规则状态历史写入doris，以便规则下线清除状态使用
        writeStateHistoryToDoris(resultDtoStreamOperator, parameterTool);
        // 将告警信息写入kafka
        writeAlertMessageToKafka(resultDtoStreamOperator, parameterTool);
        env.execute();
    }

    /**
     * 将告警信息写入kafka
     */
    private static void writeAlertMessageToKafka(SingleOutputStreamOperator<ResultDTO> resultDtoStreamOperator, ParameterTool parameterTool) {
        SingleOutputStreamOperator<String> warnMessageStreamOperator = resultDtoStreamOperator
                // 非法数据过滤
                .filter(resultDTO -> Objects.nonNull(resultDTO.getAlertMessageDTO()))
                .returns(Types.POJO(ResultDTO.class)).uid("alert-message-filter")
                // 实体类转json
                .map(resultDTO -> JsonUtils.toJsonString(resultDTO.getAlertMessageDTO()))
                .returns(Types.STRING).uid("alert-message-map");
        FlinkKafkaConnector.writer(warnMessageStreamOperator, parameterTool);
    }

    /**
     * 将规则状态历史写入doris
     */
    private static void writeStateHistoryToDoris(SingleOutputStreamOperator<ResultDTO> resultDtoStreamOperator, ParameterTool parameterTool) {
        SingleOutputStreamOperator<String> streamOperator = resultDtoStreamOperator
                // 非法数据过滤
                .filter(resultDTO -> Objects.nonNull(resultDTO.getRuleKeyHistoryDTO()))
                .returns(Types.POJO(ResultDTO.class)).uid("state-history-filter")
                // 实体类转json
                .map(resultDTO -> JsonUtils.toJsonStringWithUpperSnakeCaseKeys(resultDTO.getRuleKeyHistoryDTO()))
                .returns(Types.STRING).uid("state-history-map");
        String database = parameterTool.get(ParameterConstants.DORIS_DATABASE);
        String tableName = parameterTool.get(ParameterConstants.DORIS_TABLE_STATE);
        FlinkDorisConnector.writer(database + DefaultConstants.POINT + tableName, streamOperator, parameterTool);
    }

    /**
     * 将kafka中的事件数据同步往 doris 中留存一份
     */
    private static void writeKafkaEventToDoris(SingleOutputStreamOperator<ResultDTO> resultDtoStreamOperator, ParameterTool parameterTool) {
        SingleOutputStreamOperator<String> streamOperator = resultDtoStreamOperator
                // 非法数据过滤
                .filter(resultDTO -> Objects.nonNull(resultDTO.getKafkaEventDTO()))
                .returns(Types.POJO(ResultDTO.class)).uid("doris-event-filter")
                // 实体类转json
                .map(resultDTO -> {
                    KafkaEventDTO kafkaEventDTO = resultDTO.getKafkaEventDTO();
                    DorisEventDTO dorisEventDTO = EventConvert.INSTANCE.kafkaDto2DorisDto(kafkaEventDTO);
                    return JsonUtils.toJsonStringWithUpperSnakeCaseKeys(dorisEventDTO);// 转为大写下划线，适配doris表结构字段
                }).returns(Types.STRING).uid("toDoris-process");
        String database = parameterTool.get(ParameterConstants.DORIS_DATABASE);
        String tableName = parameterTool.get(ParameterConstants.DORIS_TABLE_EVENT);
        FlinkDorisConnector.writer(database + DefaultConstants.POINT + tableName, streamOperator, parameterTool);
    }

}
