package com.liboshuai.slr.server.biz.convert.alert;

import com.liboshuai.slr.engine.api.dto.AlertDTO;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import com.liboshuai.slr.server.biz.dal.dataobject.alert.MongoAlertDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlertConvert {

    @Mapping(source = "alertTime", target = "alertTime", qualifiedByName = "string2LocalDateTime")
    List<MongoAlertDO> batchConvertDto2Mongo(List<AlertDTO> alertDTOList);

    @Mapping(source = "alertTime", target = "alertTime", qualifiedByName = "localDateTime2String")
    List<AlertDTO> batchConvertMongo2Dto(List<MongoAlertDO> mongoAlertDOList);

    default LocalDateTime string2LocalDateTime(String string) {
        if (!StringUtils.hasText(string)) {
            return null;
        }
        return LocalDateTimeUtils.convertStr2LocalDateTime(string);
    }

    default String localDateTime2String(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        return LocalDateTimeUtils.convertLocalDateTime2Str(localDateTime);
    }
}