package com.liboshuai.slr.server.biz.dal.mysql.rule;

import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.DorisEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DorisEventMapper extends BaseMapperX<DorisEventDO> {
    default List<DorisEventDO> selectListByKey(String channel, String targetField, String eventField) {
        return selectList(new LambdaQueryWrapperX<DorisEventDO>()
                .eq(DorisEventDO::getChannel, channel)
                .eq(DorisEventDO::getTargetField, targetField)
                .eq(DorisEventDO::getEventField, eventField)
                .orderByAsc(DorisEventDO::getEventTime)
        );
    }
}
