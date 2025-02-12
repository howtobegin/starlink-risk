package com.liboshuai.slr.engine.biz.framework.connector;

import com.liboshuai.slr.engine.biz.constants.ParameterConstants;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import org.apache.doris.flink.cfg.DorisExecutionOptions;
import org.apache.doris.flink.cfg.DorisOptions;
import org.apache.doris.flink.cfg.DorisReadOptions;
import org.apache.doris.flink.sink.DorisSink;
import org.apache.doris.flink.sink.writer.SimpleStringSerializer;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;

import java.util.Properties;
import java.util.UUID;

/**
 * Flink 写入 Doris 工具类
 */
public class FlinkDorisConnector {

    /**
     * 将数据写入 Doris
     *
     * @param dataStream    数据流
     * @param parameterTool 参数工具
     */
    public static void writer(String tableName, DataStream<String> dataStream, ParameterTool parameterTool) {
        // 从参数中获取 Doris 配置
        String active = parameterTool.get(ParameterConstants.FLINK_ENV_ACTIVE);
        String host = parameterTool.get(ParameterConstants.DORIS_FE_HOST);
        String httpPort = parameterTool.get(ParameterConstants.DORIS_FE_PORT_HTTP);
        String username = parameterTool.get(ParameterConstants.DORIS_USERNAME);
        String password = parameterTool.get(ParameterConstants.DORIS_PASSWORD);

        DorisSink.Builder<String> builder = DorisSink.builder();
        DorisOptions.Builder dorisBuilder = DorisOptions.builder();
        dorisBuilder.setFenodes(host + DefaultConstants.COLON + httpPort)
                .setTableIdentifier(tableName)
                .setUsername(username)
                .setPassword(password);


        Properties properties = new Properties();
        // 上游是 json 写入时，需要开启配置
        properties.setProperty("format", "json");
        properties.setProperty("read_json_by_line", "true");
        DorisExecutionOptions.Builder executionBuilder = DorisExecutionOptions.builder();
        // 生成一个唯一的 label prefix
        String uniqueLabelPrefix = "starlink-risk-engine-" + active + "-" + UUID.randomUUID();
        executionBuilder.setLabelPrefix(uniqueLabelPrefix) //streamload label prefix
                .setDeletable(false)
                .setStreamLoadProp(properties);

        builder.setDorisReadOptions(DorisReadOptions.builder().build())
                .setDorisExecutionOptions(executionBuilder.build())
                .setSerializer(new SimpleStringSerializer()) //serialize according to string
                .setDorisOptions(dorisBuilder.build());

        dataStream.sinkTo(builder.build()).name("doris-[" + tableName + "]").uid("doris-[" + tableName + "]");
    }

}