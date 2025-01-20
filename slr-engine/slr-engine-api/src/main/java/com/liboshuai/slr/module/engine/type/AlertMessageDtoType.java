package com.liboshuai.slr.module.engine.type;

import com.liboshuai.slr.module.engine.dto.AlertMessageDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AlertMessageDtoType extends TypeInfoFactory<AlertMessageDTO> {
    @Override
    public TypeInformation<AlertMessageDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("channel", Types.STRING);
        typeInformationMap.put("ruleCode", Types.LONG);
        typeInformationMap.put("alertMessage", Types.STRING);
        typeInformationMap.put("alertTime", Types.STRING);
        typeInformationMap.put("targetField", Types.STRING);
        typeInformationMap.put("targetValue", Types.STRING);
        typeInformationMap.put("eventValueGroup", Types.MAP(Types.STRING, Types.LONG));
        return Types.POJO(AlertMessageDTO.class, typeInformationMap);
    }
}
