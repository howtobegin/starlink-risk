package com.liboshuai.slr.module.engine.framework.connector;

import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.module.engine.constants.ParameterConstants;
import com.liboshuai.slr.module.engine.dto.MysqlCdcDTO;
import com.liboshuai.slr.module.engine.framework.serialization.MysqlCdcDeserializationSchema;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
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

        MySqlSource<MysqlCdcDTO> ruleCdcSource = MySqlSource.<MysqlCdcDTO>builder()
                .hostname(hostname)
                .port(Integer.parseInt(port))
                .username(username)
                .password(password)
                .databaseList(database)
                .tableList(database + DefaultConstants.POINT + table)
                .startupOptions(StartupOptions.initial())
                .deserializer(new MysqlCdcDeserializationSchema())
                .build();
        return env.fromSource(
                ruleCdcSource, WatermarkStrategy.noWatermarks(), "MysqlCdc-[" + table + "]"
        );
//        return dataStreamSource.map(
//                json -> {
//                    log.info("MysqlCdc的json数据: {}", json);
//                    MysqlCdcDTO.MysqlCdcDTOBuilder mysqlCdcDTOBuilder = MysqlCdcDTO.builder();
//                    JsonNode jsonNode = JsonUtils.parseTree(json);
//                    log.info("jsonNode: {}", jsonNode);
//                    // 赋值 op
//                    JsonNode opJsonNode = jsonNode.get("op");
//                    log.info("opJsonNode.asText(): {}", opJsonNode.asText());
//                    mysqlCdcDTOBuilder.op(opJsonNode.asText());
//                    // 赋值 before 值
//                    JsonNode beforeJsonNode = jsonNode.get("before");
//                    log.info("beforeJsonNode: {}", JsonUtils.toJsonString(beforeJsonNode));
//                    mysqlCdcDTOBuilder.before(JsonUtils.toJsonString(beforeJsonNode));
//                    // 赋值 after 值
//                    JsonNode afterJsonNode = jsonNode.get("after");
//                    log.info("afterJsonNode: {}", JsonUtils.toJsonString(afterJsonNode));
//                    mysqlCdcDTOBuilder.after(JsonUtils.toJsonString(afterJsonNode));
//                    return mysqlCdcDTOBuilder.build();
//                }
//        );
    }
}
