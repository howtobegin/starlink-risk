package com.liboshuai.slr.module.admin.dal.mysql.riskRule;

import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleEventMapper extends BaseMapperX<RuleEventDO> {

    default List<RuleEventDO> selectListByEventCodes(List<String> eventCodeList) {
        return selectList(new LambdaQueryWrapperX<RuleEventDO>()
                .in(RuleEventDO::getEventCode, eventCodeList));
    }
}
