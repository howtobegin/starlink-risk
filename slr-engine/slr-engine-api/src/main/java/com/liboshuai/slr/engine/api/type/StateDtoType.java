package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.StateDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class StateDtoType extends TypeInfoFactory<StateDTO> {
    @Override
    public TypeInformation<StateDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("ruleCode", Types.LONG);
        typeInformationMap.put("ruleVersion", Types.LONG);
        typeInformationMap.put("channel", Types.STRING);
        typeInformationMap.put("targetField", Types.STRING);
        typeInformationMap.put("targetValue", Types.STRING);
        return Types.POJO(StateDTO.class, typeInformationMap);
    }
}
