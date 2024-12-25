package com.liboshuai.slr.module.admin.dal.mysql.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleInfoReqVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleInfoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RuleInfoMapper extends BaseMapperX<RuleInfoDO> {
    default PageResult<RuleInfoDO> selectPage(RuleInfoReqVO ruleInfoReqVO) {
        return selectPage(ruleInfoReqVO, new LambdaQueryWrapperX<RuleInfoDO>()
                .eqIfPresent(RuleInfoDO::getChannel, ruleInfoReqVO.getChannel())
                .eqIfPresent(RuleInfoDO::getRuleCode, ruleInfoReqVO.getRuleCode())
                .likeIfPresent(RuleInfoDO::getRuleName, ruleInfoReqVO.getRuleName())
                .eqIfPresent(RuleInfoDO::getRuleStatus, ruleInfoReqVO.getRuleStatus())
                .eqIfPresent(RuleInfoDO::getModelCode, ruleInfoReqVO.getModelCode())
                .orderByDesc(RuleInfoDO::getId)
        );
    }

}
