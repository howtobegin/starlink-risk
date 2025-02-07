package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.CdcSourceDTO;
import com.liboshuai.slr.engine.api.dto.MysqlCdcDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MysqlCdcDtoType extends TypeInfoFactory<MysqlCdcDTO> {
    @Override
    public TypeInformation<MysqlCdcDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("source", Types.POJO(CdcSourceDTO.class));
        typeInformationMap.put("before", Types.STRING);
        typeInformationMap.put("after", Types.STRING);
        typeInformationMap.put("op", Types.STRING);
        return Types.POJO(MysqlCdcDTO.class, typeInformationMap);
    }
}
