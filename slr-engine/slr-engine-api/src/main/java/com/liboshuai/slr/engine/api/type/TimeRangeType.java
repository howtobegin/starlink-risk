package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.TimeRangeDTO;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TimeRangeType extends TypeInfoFactory<TimeRangeDTO> {
    @Override
    public TypeInformation<TimeRangeDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("type", Types.STRING);
        typeInformationMap.put("startTime", Types.LOCAL_TIME);
        typeInformationMap.put("endTime", Types.LOCAL_TIME);
        typeInformationMap.put("daysOfWeek", Types.LIST(Types.STRING));
        typeInformationMap.put("startDayOfMonth", Types.INT);
        typeInformationMap.put("endDayOfMonth", Types.INT);
        typeInformationMap.put("startMonth", Types.INT);
        typeInformationMap.put("endMonth", Types.INT);
        typeInformationMap.put("startYearlyDate", Types.STRING);
        typeInformationMap.put("endYearlyDate", Types.STRING);
        return Types.POJO(TimeRangeDTO.class, typeInformationMap);
    }
}
