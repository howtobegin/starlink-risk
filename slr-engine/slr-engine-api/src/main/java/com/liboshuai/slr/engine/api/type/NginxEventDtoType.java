package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.NginxEventDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class NginxEventDtoType extends TypeInfoFactory<NginxEventDTO> {
    @Override
    public TypeInformation<NginxEventDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("eventTime", Types.STRING);
        typeInformationMap.put("channel", Types.STRING);
        typeInformationMap.put("targetField", Types.STRING);
        typeInformationMap.put("targetValue", Types.STRING);
        typeInformationMap.put("eventField", Types.STRING);
        typeInformationMap.put("eventValue", Types.STRING);
        typeInformationMap.put("eventAttrMap", Types.STRING);
        return Types.POJO(NginxEventDTO.class, typeInformationMap);
    }
}
