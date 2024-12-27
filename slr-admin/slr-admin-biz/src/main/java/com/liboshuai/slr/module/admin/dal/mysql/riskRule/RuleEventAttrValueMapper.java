package com.liboshuai.slr.module.admin.dal.mysql.riskRule;

import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventAttrValueDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleEventAttrValueMapper extends BaseMapperX<RuleEventAttrValueDO> {
    default List<RuleEventAttrValueDO> selectListByCondCodes(List<String> condCodeList) {
        return selectList(new LambdaQueryWrapperX<RuleEventAttrValueDO>()
                .in(RuleEventAttrValueDO::getCondCode, condCodeList));
    }

    default void deleteByCondCodes(List<String> condCodeList) {
        delete(new LambdaQueryWrapperX<RuleEventAttrValueDO>()
                .in(RuleEventAttrValueDO::getCondCode, condCodeList));
    }
}
