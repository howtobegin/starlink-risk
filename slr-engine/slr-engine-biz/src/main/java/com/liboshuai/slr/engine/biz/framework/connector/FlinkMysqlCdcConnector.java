package com.liboshuai.slr.engine.biz.framework.connector;

import com.liboshuai.slr.engine.api.dto.MysqlCdcDTO;
import com.liboshuai.slr.engine.biz.constants.ParameterConstants;
import com.liboshuai.slr.engine.biz.framework.serialization.MysqlCdcDeserializationSchema;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.util.jasypt.JasyptUtil;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
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
        String decryptedPassword = parameterTool.get(ParameterConstants.MYSQL_PASSWORD);
        String password = JasyptUtil.decrypt(decryptedPassword);
        String database = parameterTool.get(ParameterConstants.MYSQL_DATABASE);
        String serverId = parameterTool.get(ParameterConstants.MYSQL_SERVERID);
        String table = parameterTool.get(ParameterConstants.MYSQL_TABLE_RULEJSON);

        MySqlSource<MysqlCdcDTO> ruleCdcSource = MySqlSource.<MysqlCdcDTO>builder()
                .hostname(hostname)
                .port(Integer.parseInt(port))
                .username(username)
                .password(password)
                .databaseList(database)
                .tableList(database + DefaultConstants.POINT + table)
                .startupOptions(StartupOptions.initial())
                // 请勿使用 JsonDebeziumDeserializationSchema，它对于long类型的序列化会出现乱码
                .deserializer(new MysqlCdcDeserializationSchema())
                .serverId(serverId)
                .build();
        return env.fromSource(
                ruleCdcSource, WatermarkStrategy.noWatermarks(), "MysqlCdc-[" + table + "]"
        ).setParallelism(1).returns(Types.POJO(MysqlCdcDTO.class));
    }
}
