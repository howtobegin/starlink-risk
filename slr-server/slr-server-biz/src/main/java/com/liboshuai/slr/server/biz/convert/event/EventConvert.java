package com.liboshuai.slr.server.biz.convert.event;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.server.biz.controller.event.vo.KafkaEventReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventConvert {

    FlinkEventDTO convertReq2Dto(KafkaEventReqVO kafkaEventReqVO);

}