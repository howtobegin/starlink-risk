package com.liboshuai.slr.module.engine.type;

import com.liboshuai.slr.module.engine.dto.RuleJsonDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RuleJsonDtoType extends TypeInfoFactory<RuleJsonDTO> {
    @Override
    public TypeInformation<RuleJsonDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("ruleCode", Types.LONG);
        typeInformationMap.put("ruleJson", Types.STRING);
        return Types.POJO(RuleJsonDTO.class, typeInformationMap);
    }
}
