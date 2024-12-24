package com.liboshuai.starlink.slr.connector.convert;

import com.liboshuai.starlink.slr.connector.pojo.vo.kafkaEvent.KafkaEventReqVO;
import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KafkaEventConvert {
    KafkaEventConvert INSTANCE = Mappers.getMapper(KafkaEventConvert.class);

    KafkaEventDTO convertReq2Dto(KafkaEventReqVO kafkaEventReqVO);
}