package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.RuleCondDTO;
import com.liboshuai.slr.engine.api.dto.RuleInfoDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RuleInfoType extends TypeInfoFactory<RuleInfoDTO> {
    @Override
    public TypeInformation<RuleInfoDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("channel", Types.STRING);
        typeInformationMap.put("ruleCode", Types.LONG);
        typeInformationMap.put("ruleName", Types.STRING);
        typeInformationMap.put("ruleDesc", Types.STRING);
        typeInformationMap.put("ruleStatus", Types.STRING);
        typeInformationMap.put("ruleVersion", Types.LONG);
        typeInformationMap.put("alertIntervalValue", Types.LONG);
        typeInformationMap.put("alertIntervalUnit", Types.STRING);
        typeInformationMap.put("alertProjectNo", Types.STRING);
        typeInformationMap.put("alertLevel", Types.STRING);
        typeInformationMap.put("alertMessage", Types.STRING);
        typeInformationMap.put("targetCode", Types.STRING);
        typeInformationMap.put("targetField", Types.STRING);
        typeInformationMap.put("targetName", Types.STRING);
        typeInformationMap.put("modelCode", Types.LONG);
        typeInformationMap.put("modelGroovy", Types.STRING);
        typeInformationMap.put("ruleCondCombOp", Types.STRING);
        typeInformationMap.put("ruleCondGroup", Types.LIST(Types.POJO(RuleCondDTO.class)));
        return Types.POJO(RuleInfoDTO.class, typeInformationMap);
    }
}
