package com.liboshuai.slr.module.engine.type;

import com.liboshuai.slr.module.engine.dto.ProcessorDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ProcessorDtoType extends TypeInfoFactory<ProcessorDTO> {
    @Override
    public TypeInformation<ProcessorDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("eventValueGroup", Types.MAP(Types.STRING, Types.LONG));
        return Types.POJO(ProcessorDTO.class, typeInformationMap);
    }
}
