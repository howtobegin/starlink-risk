package com.liboshuai.slr.module.engine.convert;

import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import com.liboshuai.slr.module.engine.dto.DorisEventDTO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Objects;

@Mapper
public interface EventConvert {

    EventConvert INSTANCE = Mappers.getMapper(EventConvert.class);

    @Mapping(source = "eventTime", target = "eventTime", qualifiedByName = "dateTimeLongToString")
    DorisEventDTO kafkaDto2DorisDto(KafkaEventDTO kafkaEventDTO);

    @Named("dateTimeLongToString")
    default String dateTimeLongToString(Long eventTime) {
        if (Objects.isNull(eventTime)) {
            return null;
        }
        return LocalDateTimeUtils.convertTimestamp2String(eventTime);
    }
}
