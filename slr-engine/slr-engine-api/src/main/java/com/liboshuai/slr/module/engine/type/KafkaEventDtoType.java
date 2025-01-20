package com.liboshuai.slr.module.engine.type;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.dto.RuleKeyHistoryDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class KafkaEventDtoType extends TypeInfoFactory<KafkaEventDTO> {
    @Override
    public TypeInformation<KafkaEventDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("eventTime", Types.LONG);
        typeInformationMap.put("channel", Types.STRING);
        typeInformationMap.put("targetField", Types.STRING);
        typeInformationMap.put("targetValue", Types.STRING);
        typeInformationMap.put("eventField", Types.STRING);
        typeInformationMap.put("eventValue", Types.STRING);
        typeInformationMap.put("eventAttrMap", Types.MAP(Types.STRING, Types.STRING));
        typeInformationMap.put("ruleKeyHistoryDTO", Types.POJO(RuleKeyHistoryDTO.class));
        return Types.POJO(KafkaEventDTO.class, typeInformationMap);
    }
}
