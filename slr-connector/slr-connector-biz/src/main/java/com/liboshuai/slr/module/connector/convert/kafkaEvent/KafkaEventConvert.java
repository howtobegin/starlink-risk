package com.liboshuai.slr.module.connector.convert.kafkaEvent;

import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventReqVO;
import com.liboshuai.slr.module.connector.dal.dataobject.kafkaEvent.KafkaEventDO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KafkaEventConvert {

    KafkaEventDTO convertReq2Dto(KafkaEventReqVO kafkaEventReqVO);

    List<KafkaEventDO> batchConvertDto2Do(List<KafkaEventDTO> kafkaEventDTOList);

    List<KafkaEventDTO> batchConvertDo2Dto(List<KafkaEventDO> kafkaEventDOList);
}