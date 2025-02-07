package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.RuleCondDTO;
import com.liboshuai.slr.engine.api.dto.RuleEventAttrValueDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RuleCondDtoType extends TypeInfoFactory<RuleCondDTO> {
    @Override
    public TypeInformation<RuleCondDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("condCode", Types.STRING);
        typeInformationMap.put("condType", Types.STRING);
        typeInformationMap.put("windowValue", Types.LONG);
        typeInformationMap.put("windowUnit", Types.STRING);
        typeInformationMap.put("beginTime", Types.STRING);
        typeInformationMap.put("endTime", Types.STRING);
        typeInformationMap.put("threshold", Types.LONG);
        typeInformationMap.put("thresholdScaleFactor", Types.LONG);
        typeInformationMap.put("crossHistory", Types.BOOLEAN);
        typeInformationMap.put("crossHistoryTimeline", Types.STRING);
        typeInformationMap.put("ruleCode", Types.LONG);
        typeInformationMap.put("eventCode", Types.STRING);
        typeInformationMap.put("eventField", Types.STRING);
        typeInformationMap.put("eventName", Types.STRING);
        typeInformationMap.put("ruleEventAttrValueGroup", Types.LIST(Types.POJO(RuleEventAttrValueDTO.class)));
        return Types.POJO(RuleCondDTO.class, typeInformationMap);
    }
}
