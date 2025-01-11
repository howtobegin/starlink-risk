package com.liboshuai.slr.module.admin.dal.mysql.riskRule;

import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.DorisEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DorisEventMapper extends BaseMapperX<DorisEventDO> {


    default List<DorisEventDO> selectListByKey(String channel, String targetField, String eventField) {
        return selectList(new LambdaQueryWrapperX<DorisEventDO>()
                .eq(DorisEventDO::getChannel, channel)
                .eq(DorisEventDO::getTargetField, targetField)
                .eq(DorisEventDO::getEventField, eventField)
                .orderByDesc(DorisEventDO::getEventTime)
        );
    }
}
