package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.StateHistoryDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FlinkEventDtoType extends TypeInfoFactory<FlinkEventDTO> {
    @Override
    public TypeInformation<FlinkEventDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("eventId", Types.LONG);
        typeInformationMap.put("eventTime", Types.LONG);
        typeInformationMap.put("channel", Types.STRING);
        typeInformationMap.put("targetField", Types.STRING);
        typeInformationMap.put("targetValue", Types.STRING);
        typeInformationMap.put("eventField", Types.STRING);
        typeInformationMap.put("eventValue", Types.STRING);
        typeInformationMap.put("eventAttrMap", Types.MAP(Types.STRING, Types.STRING));
        typeInformationMap.put("stateHistoryDTO", Types.POJO(StateHistoryDTO.class));
        return Types.POJO(FlinkEventDTO.class, typeInformationMap);
    }
}
