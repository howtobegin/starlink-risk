package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.AlertMessageDTO;
import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.ResultDTO;
import com.liboshuai.slr.engine.api.dto.StateHistoryDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ResultDtoType extends TypeInfoFactory<ResultDTO> {
    @Override
    public TypeInformation<ResultDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("flinkEventDTO", Types.POJO(FlinkEventDTO.class));
        typeInformationMap.put("stateHistoryDTO", Types.POJO(StateHistoryDTO.class));
        typeInformationMap.put("alertMessageDTO", Types.POJO(AlertMessageDTO.class));
        return Types.POJO(ResultDTO.class, typeInformationMap);
    }
}
