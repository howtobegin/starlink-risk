package com.liboshuai.slr.module.connector.convert;

import com.liboshuai.slr.module.connector.pojo.mongo.AlertMessageMongo;
import com.liboshuai.slr.module.engine.dto.AlertMessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AlertMessageConvert {
    AlertMessageConvert INSTANCE = Mappers.getMapper(AlertMessageConvert.class);

    List<AlertMessageMongo> batchConvertDto2Mongo(List<AlertMessageDTO> alertMessageDTOList);
}