package com.liboshuai.starlink.slr.engine.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.liboshuai.starlink.slr.engine.dto.RuleCdcDTO;
import com.liboshuai.starlink.slr.engine.utils.JsonUtil;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Field;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Schema;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.data.Struct;
import com.ververica.cdc.connectors.shaded.org.apache.kafka.connect.source.SourceRecord;
import com.ververica.cdc.debezium.DebeziumDeserializationSchema;
import io.debezium.data.Envelope;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;

import java.util.List;

public class MysqlCdcDeserializationSchema implements DebeziumDeserializationSchema<RuleCdcDTO> {

    private static final long serialVersionUID = -4554108517291370408L;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取数据库名、表名
     */
    private ObjectNode getDatabaseTableJson(SourceRecord sourceRecord) {
        String topic = sourceRecord.topic();
        String[] fields = topic.split("\\.");
        String database = fields[1];
        String tableName = fields[2];
        ObjectNode ret = objectMapper.createObjectNode();
        ret.put("database", database);
        ret.put("table", tableName);
        return ret;
    }

    /**
     * 获取before、after数据
     */
    public ObjectNode getDataJson(SourceRecord sourceRecord, String fieldName) {
        Struct value = (Struct) sourceRecord.value();
        Struct struct = value.getStruct(fieldName);
        ObjectNode ret = objectMapper.createObjectNode();
        if (struct != null) {
            Schema schema = struct.schema();
            List<Field> fields = schema.fields();
            for (Field field : fields) {
                Object obj = struct.get(field);
                ret.put(field.name(), obj != null ? obj.toString() : null);
            }
        }
        return ret;
    }

    public String getOP(SourceRecord sourceRecord) {
        Envelope.Operation operation = Envelope.operationFor(sourceRecord);
        return operation.code();
    }

    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<RuleCdcDTO> collector) {
        //1.创建 JSON 对象用于存储最终数据
        ObjectNode result = objectMapper.createObjectNode();
        //2.获取库名、表名放入 source
        ObjectNode databaseTableJson = getDatabaseTableJson(sourceRecord);
        //3.获取"before"数据
        ObjectNode beforeJson = getDataJson(sourceRecord, "before");
        //4.获取"after"数据
        ObjectNode afterJson = getDataJson(sourceRecord, "after");
        //5.获取操作类型 CREATE UPDATE DELETE 进行符合 Debezium-op 的字母
        String type = getOP(sourceRecord);
        //6.将字段写入 JSON 对象
        result.set("source", databaseTableJson);
        result.set("before", beforeJson);
        result.set("after", afterJson);
        result.put("op", type);

        // 7.将 JSON 转换为 RuleCdcDTO 对象
        RuleCdcDTO ruleCdcDTO = JsonUtil.parseObjectFromUnderscore(result.toString(), RuleCdcDTO.class);
        //8.输出数据
        collector.collect(ruleCdcDTO);
    }

    @Override
    public TypeInformation<RuleCdcDTO> getProducedType() {
        // 表示返回 RuleCdcDTO 类型
        return TypeInformation.of(RuleCdcDTO.class);
    }
}