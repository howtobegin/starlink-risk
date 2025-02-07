package com.liboshuai.slr.engine.biz.framework.serialization;

import com.liboshuai.slr.engine.api.dto.CdcSourceDTO;
import com.liboshuai.slr.engine.api.dto.MysqlCdcDTO;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Field;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Schema;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Struct;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.json.JsonConverter;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.source.SourceRecord;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class MysqlCdcDeserializationSchema implements DebeziumDeserializationSchema<MysqlCdcDTO> {

    private static final long serialVersionUID = -4554108517291370408L;

    // JsonConverter
    private static final JsonConverter jsonConverter = new JsonConverter();

    static {
        // 配置 JsonConverter，不输出 Schema
        Map<String, Object> configs = new HashMap<>();
        configs.put("schemas.enable", false);
        jsonConverter.configure(configs, false);
    }

    /**
     * 获取数据库名、表名
     */
    private CdcSourceDTO getSource(SourceRecord sourceRecord) {
        String topic = sourceRecord.topic();
        String[] fields = topic.split("\\.");
        String database = fields[1];
        String tableName = fields[2];
        return new CdcSourceDTO(database, tableName);
    }

    /**
     * 使用 JsonConverter 获取 before、after 数据的 JSON 字符串
     */
    private String getDataJsonAsString(SourceRecord sourceRecord, String fieldName) {
        Struct value = (Struct) sourceRecord.value();
        Struct struct = value.getStruct(fieldName);
        Map<String, String> structMap = new HashMap<>();
        if (struct != null) {
            // 获取Struct中包含所有字段名，遍历即可
            Schema schema = struct.schema();
            for (Field field : schema.fields()) {
                String k = field.name();
                String v = Objects.isNull(struct.get(field)) ? null : struct.get(field).toString();
                structMap.put(k, v);
            }
            return JsonUtils.toCamelCaseJson(structMap);
        }
        return null;
    }

    /**
     * 获取操作类型
     */
    private String getOP(SourceRecord sourceRecord) {
        Envelope.Operation operation = Envelope.operationFor(sourceRecord);
        return operation.code();
    }

    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<MysqlCdcDTO> collector) {
        // 创建 MysqlCdcDTO 对象
        MysqlCdcDTO mysqlCdcDTO = new MysqlCdcDTO();
        // 获取 库名、表名信息
        mysqlCdcDTO.setSource(getSource(sourceRecord));
        // 获取 before、after 数据的 JSON 字符串
        mysqlCdcDTO.setBefore(getDataJsonAsString(sourceRecord, "before"));
        mysqlCdcDTO.setAfter(getDataJsonAsString(sourceRecord, "after"));
        // 设置操作类型
        mysqlCdcDTO.setOp(getOP(sourceRecord));
        // 输出数据
        collector.collect(mysqlCdcDTO);
    }

    @Override
    public TypeInformation<MysqlCdcDTO> getProducedType() {
        // 表示返回 MysqlCdcDTO 类型
        return TypeInformation.of(MysqlCdcDTO.class);
    }
}