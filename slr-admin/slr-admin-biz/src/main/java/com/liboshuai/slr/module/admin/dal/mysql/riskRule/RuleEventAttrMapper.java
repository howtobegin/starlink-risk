package com.liboshuai.slr.module.admin.dal.mysql.riskRule;

import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventAttrDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleEventAttrMapper extends BaseMapperX<RuleEventAttrDO> {
    default List<RuleEventAttrDO> selectListByEventCodes(List<String> eventCodeList) {
        return selectList(new LambdaQueryWrapperX<RuleEventAttrDO>()
                .in(RuleEventAttrDO::getEventCode, eventCodeList));
    }
}
