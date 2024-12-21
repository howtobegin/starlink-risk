package com.liboshuai.starlink.slr.admin.convert.risk;

import com.liboshuai.starlink.slr.admin.pojo.entity.risk.RuleInfoEntity;
import com.liboshuai.starlink.slr.admin.pojo.vo.risk.RuleInfoVO;
import com.liboshuai.starlink.slr.engine.api.dto.RuleInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RuleInfoConvert {

    RuleInfoConvert INSTANCE = Mappers.getMapper(RuleInfoConvert.class);

    RuleInfoEntity vo2Entity(RuleInfoVO ruleInfoVO);

    RuleInfoVO entity2Vo(RuleInfoEntity ruleInfoEntity);

    List<RuleInfoEntity> batchVo2Entity(List<RuleInfoVO> ruleInfoVOList);

    List<RuleInfoVO> batchEntity2Vo(List<RuleInfoEntity> ruleInfoEntityList);

    RuleInfoDTO entity2Dto(RuleInfoEntity ruleInfoEntity);

    List<RuleInfoDTO> batchEntity2Dto(List<RuleInfoEntity> ruleInfoEntityList);
}