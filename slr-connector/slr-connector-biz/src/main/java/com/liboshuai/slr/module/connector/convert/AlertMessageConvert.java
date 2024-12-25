package com.liboshuai.slr.module.connector.convert;

import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.AlertMessageDO;
import com.liboshuai.slr.module.engine.dto.AlertMessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlertMessageConvert {

    List<AlertMessageDO> batchConvertDto2Mongo(List<AlertMessageDTO> alertMessageDTOList);
}