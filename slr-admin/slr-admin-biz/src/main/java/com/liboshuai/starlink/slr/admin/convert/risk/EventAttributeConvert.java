package com.liboshuai.starlink.slr.admin.convert.risk;

import com.liboshuai.starlink.slr.admin.pojo.entity.risk.EventAttributeEntity;
import com.liboshuai.starlink.slr.admin.pojo.vo.risk.EventAttributeVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface EventAttributeConvert {

    EventAttributeConvert INSTANCE = Mappers.getMapper(EventAttributeConvert.class);

    EventAttributeEntity vo2Entity(EventAttributeVO eventAttributeVO);

    EventAttributeVO entity2Vo(EventAttributeEntity eventAttributeEntity);

    List<EventAttributeEntity> batchVo2Entity(List<EventAttributeVO> eventAttributeVOList);

    List<EventAttributeVO> batchEntity2Vo(List<EventAttributeEntity> eventAttributeEntityList);

//    EventAttributeDTO entity2Dto(EventAttributeEntity eventAttributeEntity);
//
//    List<EventAttributeDTO> batchEntity2Dto(List<EventAttributeEntity> eventAttributeEntityList);
}