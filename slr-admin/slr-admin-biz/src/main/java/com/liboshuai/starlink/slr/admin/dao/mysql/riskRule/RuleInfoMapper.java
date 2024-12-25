package com.liboshuai.starlink.slr.admin.dao.mysql.riskRule;

import com.liboshuai.starlink.slr.admin.pojo.entity.riskRule.RuleInfoEntity;
import com.liboshuai.starlink.slr.admin.pojo.vo.riskRule.RuleInfoReqVO;
import com.liboshuai.starlink.slr.framework.common.pojo.PageResult;
import com.liboshuai.starlink.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.starlink.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RuleInfoMapper extends BaseMapperX<RuleInfoEntity> {
    default PageResult<RuleInfoEntity> selectPage(RuleInfoReqVO ruleInfoReqVO) {
        return selectPage(ruleInfoReqVO, new LambdaQueryWrapperX<RuleInfoEntity>()
                .eqIfPresent(RuleInfoEntity::getChannel, ruleInfoReqVO.getChannel())
                .eqIfPresent(RuleInfoEntity::getRuleCode, ruleInfoReqVO.getRuleCode())
                .likeIfPresent(RuleInfoEntity::getRuleName, ruleInfoReqVO.getRuleName())
                .eqIfPresent(RuleInfoEntity::getRuleStatus, ruleInfoReqVO.getRuleStatus())
                .eqIfPresent(RuleInfoEntity::getModelCode, ruleInfoReqVO.getModelCode())
                .orderByDesc(RuleInfoEntity::getId)
        );
    }

}
