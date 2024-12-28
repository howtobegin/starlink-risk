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

    default RuleEventDO selectOneByEventCode(String eventCode) {
        return selectOne(new LambdaQueryWrapperX<RuleEventDO>()
                .eq(RuleEventDO::getEventCode, eventCode));
    }

    default List<RuleEventDO> selectListByKeyCode(String keyCode) {
        return selectList(new LambdaQueryWrapperX<RuleEventDO>()
                .eq(RuleEventDO::getKeyCode, keyCode));
    }

    default List<RuleEventDO> selectListByNotInIds(List<Long> ruleEventIdList) {
        return selectList(new LambdaQueryWrapperX<RuleEventDO>()
                .notIn(RuleEventDO::getId, ruleEventIdList));
    }

    default void deleteByKeyCode(String keyCode) {
        delete(new LambdaQueryWrapperX<RuleEventDO>()
                .eq(RuleEventDO::getKeyCode, keyCode));
    }

    default List<RuleEventDO> selectListByNeId(Long eventId) {
        return selectList(new LambdaQueryWrapperX<RuleEventDO>()
                .ne(RuleEventDO::getId, eventId));
    }
}
