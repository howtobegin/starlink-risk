package com.liboshuai.slr.module.engine.type;

import com.liboshuai.slr.module.engine.dto.RuleKeyHistoryDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RuleKeyHistoryDtoType extends TypeInfoFactory<RuleKeyHistoryDTO> {
    @Override
    public TypeInformation<RuleKeyHistoryDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("ruleCode", Types.LONG);
        typeInformationMap.put("ruleVersion", Types.LONG);
        typeInformationMap.put("channel", Types.STRING);
        typeInformationMap.put("targetField", Types.STRING);
        typeInformationMap.put("targetValue", Types.STRING);
        return Types.POJO(RuleKeyHistoryDTO.class, typeInformationMap);
    }
}
