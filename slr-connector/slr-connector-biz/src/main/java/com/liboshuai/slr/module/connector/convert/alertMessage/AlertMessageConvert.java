package com.liboshuai.slr.module.connector.convert.alertMessage;

import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageApiDTO;
import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.AlertMessageDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlertMessageConvert {

    @Mapping(source = "alertTime", target = "alertTime", qualifiedByName = "string2LocalDateTime")
    List<AlertMessageDO> batchConvertDto2Mongo(List<AlertMessageApiDTO> alertMessageApiDTOList);

    @Mapping(source = "alertTime", target = "alertTime", qualifiedByName = "localDateTime2String")
    List<AlertMessageApiDTO> batchConvertMongo2Dto(List<AlertMessageDO> alertMessageDOList);

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