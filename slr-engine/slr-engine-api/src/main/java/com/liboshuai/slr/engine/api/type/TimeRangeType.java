package com.liboshuai.slr.engine.api.type;

import com.liboshuai.slr.engine.api.dto.TimeRangeDTO;
import com.liboshuai.slr.engine.api.enums.TimeRangeEnum;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.Map;

public class TimeRangeType extends TypeInfoFactory<TimeRangeDTO> {
    @Override
    public TypeInformation<TimeRangeDTO> createTypeInfo(Type type, Map<String, TypeInformation<?>> map) {
        Map<String, TypeInformation<?>> typeInformationMap = new HashMap<>();
        typeInformationMap.put("type", Types.POJO(TimeRangeEnum.class));
        typeInformationMap.put("startTime", Types.LOCAL_TIME);
        typeInformationMap.put("endTime", Types.LOCAL_TIME);
        typeInformationMap.put("daysOfWeek", Types.LIST(Types.POJO(DayOfWeek.class)));
        typeInformationMap.put("startDayOfMonth", Types.INT);
        typeInformationMap.put("endDayOfMonth", Types.INT);
        typeInformationMap.put("startMonth", Types.INT);
        typeInformationMap.put("endMonth", Types.INT);
        typeInformationMap.put("startYearlyDate", Types.POJO(MonthDay.class));
        typeInformationMap.put("endYearlyDate", Types.POJO(MonthDay.class));
        return Types.POJO(TimeRangeDTO.class, typeInformationMap);
    }
}
