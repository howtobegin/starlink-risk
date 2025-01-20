package com.liboshuai.slr.module.engine.framework.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.module.engine.constants.ParameterConstants;
import com.liboshuai.slr.module.engine.dto.MysqlCdcDTO;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * MysqlCdc 数据源
 **/
@Slf4j
public class FlinkMysqlCdcConnector {

    /**
     * MySql cdc 读取数据
     */
    public static DataStream<MysqlCdcDTO> read(
            StreamExecutionEnvironment env,
            ParameterTool parameterTool) {

        String hostname = parameterTool.get(ParameterConstants.MYSQL_HOSTNAME);
        String port = parameterTool.get(ParameterConstants.MYSQL_PORT);
        String username = parameterTool.get(ParameterConstants.MYSQL_USERNAME);
//        String password = CryptoUtils.decrypt(parameterTool.get(ParameterConstants.MYSQL_PASSWORD));
        String password = parameterTool.get(ParameterConstants.MYSQL_PASSWORD);
        String database = parameterTool.get(ParameterConstants.MYSQL_DATABASE);
        String table = parameterTool.get(ParameterConstants.MYSQL_TABLE_RULEJSON);

        MySqlSource<String> ruleCdcSource = MySqlSource.<String>builder()
                .hostname(hostname)
                .port(Integer.parseInt(port))
                .username(username)
                .password(password)
                .databaseList(database)
                .tableList(database + DefaultConstants.POINT + table)
                .startupOptions(StartupOptions.initial())
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();
        DataStreamSource<String> dataStreamSource = env.fromSource(
                ruleCdcSource, WatermarkStrategy.noWatermarks(), "MysqlCdc-[" + table + "]"
        );
        return dataStreamSource.map(
                json -> {
                    log.info("MysqlCdc的json数据: {}", json);
                    MysqlCdcDTO.MysqlCdcDTOBuilder mysqlCdcDTOBuilder = MysqlCdcDTO.builder();
                    JsonNode jsonNode = JsonUtils.parseTree(json);
                    // 赋值 op
                    JsonNode opJsonNode = jsonNode.get("op");
                    log.info("opJsonNode.asText(): {}", opJsonNode.asText());
                    mysqlCdcDTOBuilder.op(opJsonNode.asText());
                    // 赋值 before 值
                    JsonNode beforeJsonNode = jsonNode.get("before");
                    log.info("beforeJsonNode.asText(): {}", beforeJsonNode.asText());
                    mysqlCdcDTOBuilder.before(beforeJsonNode.asText());
                    // 赋值 after 值
                    JsonNode afterJsonNode = jsonNode.get("after");
                    log.info("afterJsonNode.asText(): {}", afterJsonNode.asText());
                    mysqlCdcDTOBuilder.after(afterJsonNode.asText());
                    return mysqlCdcDTOBuilder.build();
                }
        );
    }
}
