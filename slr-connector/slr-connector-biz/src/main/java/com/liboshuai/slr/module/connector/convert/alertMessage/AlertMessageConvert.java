package com.liboshuai.slr.module.connector.convert.alertMessage;

import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageApiDTO;
import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.AlertMessageDO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlertMessageConvert {

    List<AlertMessageDO> batchConvertDto2Mongo(List<AlertMessageApiDTO> alertMessageApiDTOList);

    List<AlertMessageApiDTO> batchConvertMongo2Dto(List<AlertMessageDO> alertMessageDOList);
}