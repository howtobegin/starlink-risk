package com.liboshuai.slr.module.admin.dal.mysql.riskRule;

import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.KafkaEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DorisEventMapper extends BaseMapperX<KafkaEventDO> {


    default List<KafkaEventDO> selectListByKey(String channel, String targetField, String eventField) {
        return selectList(new LambdaQueryWrapperX<KafkaEventDO>()
                .eq(KafkaEventDO::getChannel, channel)
                .eq(KafkaEventDO::getTargetField, targetField)
                .eq(KafkaEventDO::getEventField, eventField)
                .orderByDesc(KafkaEventDO::getEventTime)
        );
    }
}
