package com.liboshuai.starlink.slr.admin.convert.risk;

import com.liboshuai.starlink.slr.admin.pojo.entity.risk.EventInfoEntity;
import com.liboshuai.starlink.slr.admin.pojo.vo.risk.EventInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface EventInfoConvert {

    EventInfoConvert INSTANCE = Mappers.getMapper(EventInfoConvert.class);

    EventInfoEntity vo2Entity(EventInfoVO eventInfoVO);

    EventInfoVO entity2Vo(EventInfoEntity eventInfoEntity);

    List<EventInfoEntity> batchVo2Entity(List<EventInfoVO> eventInfoVOList);

    List<EventInfoVO> batchEntity2Vo(List<EventInfoEntity> eventInfoEntityList);

//    EventInfoDTO entity2Dto(EventInfoEntity eventInfoEntity);
//
//    List<EventInfoDTO> batchEntity2Dto(List<EventInfoEntity> eventInfoEntityList);
}