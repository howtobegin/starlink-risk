package com.liboshuai.slr.module.admin.dal.mysql.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoPageReqVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleInfoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RuleInfoMapper extends BaseMapperX<RuleInfoDO> {
    default PageResult<RuleInfoDO> selectPage(RuleInfoPageReqVO ruleInfoPageReqVO) {
        return selectPage(ruleInfoPageReqVO, new LambdaQueryWrapperX<RuleInfoDO>()
                .eqIfPresent(RuleInfoDO::getChannel, ruleInfoPageReqVO.getChannel())
                .eqIfPresent(RuleInfoDO::getRuleCode, ruleInfoPageReqVO.getRuleCode())
                .likeIfPresent(RuleInfoDO::getRuleName, ruleInfoPageReqVO.getRuleName())
                .eqIfPresent(RuleInfoDO::getRuleStatus, ruleInfoPageReqVO.getRuleStatus())
                .eqIfPresent(RuleInfoDO::getModelCode, ruleInfoPageReqVO.getModelCode())
                .orderByDesc(RuleInfoDO::getId)
        );
    }

    default RuleInfoDO selectOneByRuleCode(String ruleCode) {
        return selectOne(new LambdaQueryWrapperX<RuleInfoDO>()
                .eq(RuleInfoDO::getRuleCode, ruleCode)
        );
    }
}
