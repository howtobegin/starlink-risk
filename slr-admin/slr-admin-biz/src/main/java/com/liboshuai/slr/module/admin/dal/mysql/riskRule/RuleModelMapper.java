package com.liboshuai.slr.module.admin.dal.mysql.riskRule;

import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleModelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleModelMapper extends BaseMapperX<RuleModelDO> {

    default List<RuleModelDO> selectListByModelCodes(List<String> modelCodeList) {
        return selectList(new LambdaQueryWrapperX<RuleModelDO>()
                .in(RuleModelDO::getModelCode, modelCodeList));
    }

    default RuleModelDO selectOneByModelCode(String modelCode) {
        return selectOne(new LambdaQueryWrapperX<RuleModelDO>()
                .eq(RuleModelDO::getModelCode, modelCode));
    }
}
