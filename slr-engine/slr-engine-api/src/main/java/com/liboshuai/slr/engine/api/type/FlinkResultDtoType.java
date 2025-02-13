package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.AlertDTO;
import com.liboshuai.slr.engine.api.dto.FlinkResultDTO;
import com.liboshuai.slr.engine.api.dto.StateDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FlinkResultDtoType extends TypeInfoFactory<FlinkResultDTO> {
    @Override
    public TypeInformation<FlinkResultDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("stateDTO", Types.POJO(StateDTO.class));
        typeInformationMap.put("alertDTO", Types.POJO(AlertDTO.class));
        return Types.POJO(FlinkResultDTO.class, typeInformationMap);
    }
}
