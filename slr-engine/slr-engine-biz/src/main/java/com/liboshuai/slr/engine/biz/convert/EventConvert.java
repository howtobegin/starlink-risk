package com.liboshuai.slr.engine.biz.convert;

import com.liboshuai.slr.engine.api.dto.DorisEventDTO;
import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import org.apache.flink.util.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Objects;

@Mapper
public interface EventConvert {

    EventConvert INSTANCE = Mappers.getMapper(EventConvert.class);

    @Mapping(source = "eventTime", target = "eventTime", qualifiedByName = "removeDecimalPoint")
    FlinkEventDTO kafkaDto2FlinkDto(KafkaEventDTO kafkaEventDTO);

    @Mapping(source = "eventTime", target = "eventTime", qualifiedByName = "dateTimeLongToString")
    DorisEventDTO flinkDto2DorisDto(FlinkEventDTO flinkEventDTO);

    @Named("removeDecimalPoint")
    default Long removeDecimalPoint(String eventTime) {
        if (StringUtils.isNullOrWhitespaceOnly(eventTime)) {
            return null;
        }
        return Long.parseLong(eventTime.replace(".", ""));
    }

    @Named("dateTimeLongToString")
    default String dateTimeLongToString(Long eventTime) {
        if (Objects.isNull(eventTime)) {
            return null;
        }
        return LocalDateTimeUtils.convertTimestamp2String(eventTime);
    }
}
