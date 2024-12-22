package com.liboshuai.starlink.slr.engine.convert;

import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.starlink.slr.engine.dto.WarnInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EventKeyConvert {
    EventKeyConvert INSTANCE = Mappers.getMapper(EventKeyConvert.class);

    WarnInfoDTO eventKafkaDTO2WarnInfoDTO(KafkaEventDTO kafkaEventDTO);
}