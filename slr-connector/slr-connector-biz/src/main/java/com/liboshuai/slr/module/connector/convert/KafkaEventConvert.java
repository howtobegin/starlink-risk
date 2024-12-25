package com.liboshuai.slr.module.connector.convert;

import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventReqVO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KafkaEventConvert {

    KafkaEventDTO convertReq2Dto(KafkaEventReqVO kafkaEventReqVO);
}