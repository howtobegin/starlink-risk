package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.RuleEventAttrValueDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RuleEventAttrValueDtoType extends TypeInfoFactory<RuleEventAttrValueDTO> {
    @Override
    public TypeInformation<RuleEventAttrValueDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("attrCode", Types.STRING);
        typeInformationMap.put("attrField", Types.STRING);
        typeInformationMap.put("attrName", Types.STRING);
        typeInformationMap.put("attrType", Types.STRING);
        typeInformationMap.put("attrOp", Types.STRING);
        typeInformationMap.put("attrValue", Types.STRING);
        typeInformationMap.put("condCode", Types.STRING);
        return Types.POJO(RuleEventAttrValueDTO.class, typeInformationMap);
    }
}
