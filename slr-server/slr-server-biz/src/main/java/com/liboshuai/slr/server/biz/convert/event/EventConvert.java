package com.liboshuai.slr.server.biz.convert.event;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.server.biz.controller.event.vo.KafkaEventReqVO;
import com.liboshuai.slr.server.biz.dal.dataobject.event.MongoEventDO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventConvert {

    FlinkEventDTO convertReq2Dto(KafkaEventReqVO kafkaEventReqVO);

    List<MongoEventDO> batchConvertDto2Do(List<FlinkEventDTO> flinkEventDTOList);

    List<FlinkEventDTO> batchConvertDo2Dto(List<MongoEventDO> mongoEventDOList);
}