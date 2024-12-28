package com.liboshuai.slr.module.admin.dal.mysql.riskRule;


import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeyPageReqVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleKeyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleKeyMapper extends BaseMapperX<RuleKeyDO> {

    default PageResult<RuleKeyDO> selectPage(RuleKeyPageReqVO ruleKeyPageReqVO) {
        return selectPage(ruleKeyPageReqVO, new LambdaQueryWrapperX<RuleKeyDO>()
                .eqIfPresent(RuleKeyDO::getChannel, ruleKeyPageReqVO.getChannel())
                .eqIfPresent(RuleKeyDO::getKeyCode, ruleKeyPageReqVO.getKeyCode())
                .likeIfPresent(RuleKeyDO::getKeyName, ruleKeyPageReqVO.getKeyName())
                .likeIfPresent(RuleKeyDO::getKeyDesc, ruleKeyPageReqVO.getKeyDesc())
                .orderByDesc(RuleKeyDO::getKeyCode)
        );
    }

    default RuleKeyDO selectOneByKeyCode(String keyCode) {
        return selectOne(new LambdaQueryWrapperX<RuleKeyDO>()
                .eq(RuleKeyDO::getKeyCode, keyCode)
        );
    }

    default List<RuleKeyDO> selectOneByNeId(Long id) {
        return selectList(new LambdaQueryWrapperX<RuleKeyDO>()
                .ne(RuleKeyDO::getId, id)
        );
    }
}
