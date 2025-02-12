package com.liboshuai.slr.server.biz.convert.kafkaEvent;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.server.biz.controller.kafkaEvent.vo.KafkaEventReqVO;
import com.liboshuai.slr.server.biz.dal.dataobject.kafkaEvent.KafkaEventDO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KafkaEventConvert {

    FlinkEventDTO convertReq2Dto(KafkaEventReqVO kafkaEventReqVO);

    List<KafkaEventDO> batchConvertDto2Do(List<FlinkEventDTO> flinkEventDTOList);

    List<FlinkEventDTO> batchConvertDo2Dto(List<KafkaEventDO> kafkaEventDOList);
}