package com.liboshuai.slr.module.admin.dal.mysql.riskRule;


import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeyPageReqVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleTargetDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleKeyMapper extends BaseMapperX<RuleTargetDO> {

    default PageResult<RuleTargetDO> selectPage(RuleKeyPageReqVO ruleKeyPageReqVO) {
        return selectPage(ruleKeyPageReqVO, new LambdaQueryWrapperX<RuleTargetDO>()
                .eqIfPresent(RuleTargetDO::getChannel, ruleKeyPageReqVO.getChannel())
                .eqIfPresent(RuleTargetDO::getTargetCode, ruleKeyPageReqVO.getKeyCode())
                .likeIfPresent(RuleTargetDO::getTargetName, ruleKeyPageReqVO.getKeyName())
                .likeIfPresent(RuleTargetDO::getTargetDesc, ruleKeyPageReqVO.getKeyDesc())
                .orderByDesc(RuleTargetDO::getId)
        );
    }

    default RuleTargetDO selectOneByKeyCode(String keyCode) {
        return selectOne(new LambdaQueryWrapperX<RuleTargetDO>()
                .eq(RuleTargetDO::getTargetCode, keyCode)
        );
    }

    default List<RuleTargetDO> selectOneByNeId(Long id) {
        return selectList(new LambdaQueryWrapperX<RuleTargetDO>()
                .ne(RuleTargetDO::getId, id)
        );
    }
}
