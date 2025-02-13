package com.liboshuai.slr.server.biz.dal.mysql.rule;

import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.RuleEventDO;
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

    default List<RuleEventDO> selectListByNotInIds(List<Long> ruleEventIdList) {
        return selectList(new LambdaQueryWrapperX<RuleEventDO>()
                .notIn(RuleEventDO::getId, ruleEventIdList));
    }

    default void deleteByKeyCode(String keyCode) {
        delete(new LambdaQueryWrapperX<RuleEventDO>()
                .eq(RuleEventDO::getTargetCode, keyCode));
    }

    default List<RuleEventDO> selectListByNeId(Long eventId) {
        return selectList(new LambdaQueryWrapperX<RuleEventDO>()
                .ne(RuleEventDO::getId, eventId));
    }

    default void updateByEventCode(RuleEventDO ruleEventDO, String eventCode) {
        update(ruleEventDO, new LambdaQueryWrapperX<RuleEventDO>()
                .eq(RuleEventDO::getEventCode, eventCode));
    }

    default List<RuleEventDO> selectListByTargetCode(String targetCode) {
        return selectList(new LambdaQueryWrapperX<RuleEventDO>()
                .eq(RuleEventDO::getTargetCode, targetCode));
    }

    default List<RuleEventDO> selectListByTargetCodeAndStatus(String targetCode, List<String> eventStatusCodeList) {
        return selectList(new LambdaQueryWrapperX<RuleEventDO>()
                .eq(RuleEventDO::getTargetCode, targetCode)
                .in(RuleEventDO::getEventStatus, eventStatusCodeList));
    }
}
