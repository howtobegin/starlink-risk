package com.liboshuai.slr.module.engine.framework.connector;

import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.module.engine.constants.ParameterConstants;
import com.liboshuai.slr.module.engine.dto.RuleCdcDTO;
import com.liboshuai.slr.module.engine.framework.serialize.MysqlCdcDeserializationSchema;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Author: liboshuai
 * @Date: 2023-10-25 13:11
 **/
public class FlinkMysqlConnector {

    /**
     * MySql cdc 读取数据
     */
    public static DataStream<RuleCdcDTO> read(
            StreamExecutionEnvironment env,
            ParameterTool parameterTool) {

        String hostname = parameterTool.get(ParameterConstants.MYSQL_HOSTNAME);
        String port = parameterTool.get(ParameterConstants.MYSQL_PORT);
        String username = parameterTool.get(ParameterConstants.MYSQL_USERNAME);
//        String password = CryptoUtils.decrypt(parameterTool.get(ParameterConstants.MYSQL_PASSWORD));
        String password = parameterTool.get(ParameterConstants.MYSQL_PASSWORD);
        String database = parameterTool.get(ParameterConstants.MYSQL_DATABASE);
        String table = parameterTool.get(ParameterConstants.MYSQL_TABLE_RULEJSON);

        MySqlSource<RuleCdcDTO> ruleCdcSource = MySqlSource.<RuleCdcDTO>builder()
                .hostname(hostname)
                .port(Integer.parseInt(port))
                .username(username)
                .password(password)
                .databaseList(database)
                .tableList(database + DefaultConstants.POINT + table)
                .startupOptions(StartupOptions.initial())
                .deserializer(new MysqlCdcDeserializationSchema())
                .build();

        return env.fromSource(ruleCdcSource, WatermarkStrategy.noWatermarks(), "ruleSource");
    }
}
