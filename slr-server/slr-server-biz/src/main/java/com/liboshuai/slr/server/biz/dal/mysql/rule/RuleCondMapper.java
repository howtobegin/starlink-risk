package com.liboshuai.slr.server.biz.dal.mysql.rule;


import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.RuleCondDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleCondMapper extends BaseMapperX<RuleCondDO> {

    default List<RuleCondDO> selectListByRuleCode(Long ruleCode) {
        return selectList(new LambdaQueryWrapperX<RuleCondDO>()
                .eq(RuleCondDO::getRuleCode, ruleCode));
    }

    default void deleteByRuleCode(Long ruleCode) {
        delete(new LambdaQueryWrapperX<RuleCondDO>()
                .eq(RuleCondDO::getRuleCode, ruleCode));
    }
}
