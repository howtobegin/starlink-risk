package com.liboshuai.starlink.slr.admin.convert.risk;

import com.liboshuai.starlink.slr.admin.pojo.entity.risk.RuleConditionEntity;
import com.liboshuai.starlink.slr.admin.pojo.vo.risk.RuleConditionVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RuleConditionConvert {

    RuleConditionConvert INSTANCE = Mappers.getMapper(RuleConditionConvert.class);

    RuleConditionEntity vo2Entity(RuleConditionVO ruleConditionVO);

    RuleConditionVO entity2Vo(RuleConditionEntity ruleConditionEntity);

    List<RuleConditionEntity> batchVo2Entity(List<RuleConditionVO> ruleConditionVOList);

    List<RuleConditionVO> batchEntity2Vo(List<RuleConditionEntity> ruleConditionEntityList);

    RuleConditionDTO entity2Dto(RuleConditionEntity ruleConditionEntity);

    List<RuleConditionDTO> batchEntity2Dto(List<RuleConditionEntity> ruleConditionEntityList);
}