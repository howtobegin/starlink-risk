package com.liboshuai.slr.module.connector.convert;

import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventReqVO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface KafkaEventConvert {
    KafkaEventConvert INSTANCE = Mappers.getMapper(KafkaEventConvert.class);

    KafkaEventDTO convertReq2Dto(KafkaEventReqVO kafkaEventReqVO);
}