package com.liboshuai.slr.module.admin.dal.mysql.riskRule;


import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleCondDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleCondMapper extends BaseMapperX<RuleCondDO> {

    default List<RuleCondDO> selectListByRuleCode(String ruleCode) {
        return selectList(new LambdaQueryWrapperX<RuleCondDO>()
                .eq(RuleCondDO::getRuleCode, ruleCode));
    }

    default void deleteByRuleCode(String ruleCode) {
        delete(new LambdaQueryWrapperX<RuleCondDO>()
                .eq(RuleCondDO::getRuleCode, ruleCode));
    }
}
