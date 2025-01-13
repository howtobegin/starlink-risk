package com.liboshuai.slr.module.admin.convert.riskRule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.DorisEventDO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DorisEventConvert {

    @Mapping(source = "eventTime", target = "eventTime", qualifiedByName = "localDateTimeToLong")
    @Mapping(source = "eventAttrMap", target = "eventAttrMap", qualifiedByName = "stringToMap")
    List<KafkaEventDTO> batchConvertDO2KafkaDTO(List<DorisEventDO> dorisEventDOList);

    default Map<String, String> stringToMap(String eventAttrMap) {
        if (!StringUtils.hasText(eventAttrMap)) {
            return Collections.emptyMap();
        }
        return JsonUtils.parseObjectWithType(eventAttrMap, new TypeReference<Map<String, String>>() {
        });
    }

    default Long localDateTimeToLong(LocalDateTime eventTime) {
        if (Objects.isNull(eventTime)) {
            return null;
        }
        return LocalDateTimeUtils.convertLocalDateTime2Timestamp(eventTime);
    }
}