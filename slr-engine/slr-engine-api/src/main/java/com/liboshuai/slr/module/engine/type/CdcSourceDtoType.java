package com.liboshuai.slr.module.engine.type;

import com.liboshuai.slr.module.engine.dto.CdcSourceDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CdcSourceDtoType extends TypeInfoFactory<CdcSourceDTO> {
    @Override
    public TypeInformation<CdcSourceDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("database", Types.STRING);
        typeInformationMap.put("table", Types.STRING);
        return Types.POJO(CdcSourceDTO.class, typeInformationMap);
    }
}
