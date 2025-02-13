package com.liboshuai.slr.server.biz.dal.mysql.rule;

import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.RuleEventAttrValueDO;
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
