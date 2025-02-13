package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.AlertDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AlertDtoType extends TypeInfoFactory<AlertDTO> {
    @Override
    public TypeInformation<AlertDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("channel", Types.STRING);
        typeInformationMap.put("ruleCode", Types.LONG);
        typeInformationMap.put("message", Types.STRING);
        typeInformationMap.put("time", Types.STRING);
        typeInformationMap.put("targetField", Types.STRING);
        typeInformationMap.put("targetValue", Types.STRING);
        typeInformationMap.put("eventValueGroup", Types.MAP(Types.STRING, Types.LONG));
        return Types.POJO(AlertDTO.class, typeInformationMap);
    }
}
