package com.liboshuai.slr.server.biz.dal.mysql.rule;


import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.RuleCondTimeRangeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleCondTimeRangeMapper extends BaseMapperX<RuleCondTimeRangeDO> {


    default void deleteByCondCodes(List<String> ruleCondCodeList) {
        delete(new LambdaQueryWrapperX<RuleCondTimeRangeDO>()
                .in(RuleCondTimeRangeDO::getCondCode, ruleCondCodeList));
    }

    default List<RuleCondTimeRangeDO> selectListByCondCodes(List<String> condCodeList) {
        return selectList(new LambdaQueryWrapperX<RuleCondTimeRangeDO>()
                .in(RuleCondTimeRangeDO::getCondCode, condCodeList));
    }
}
