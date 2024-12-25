package com.liboshuai.slr.module.admin.dao.mysql.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.pojo.entity.riskRule.RuleInfoEntity;
import com.liboshuai.slr.module.admin.pojo.vo.riskRule.RuleInfoReqVO;
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
