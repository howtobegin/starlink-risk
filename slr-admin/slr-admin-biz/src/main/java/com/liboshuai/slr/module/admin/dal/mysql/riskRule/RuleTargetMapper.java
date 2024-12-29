package com.liboshuai.slr.module.admin.dal.mysql.riskRule;


import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.mybatis.core.mapper.BaseMapperX;
import com.liboshuai.slr.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleTargetPageReqVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleTargetDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleTargetMapper extends BaseMapperX<RuleTargetDO> {

    default PageResult<RuleTargetDO> selectPage(RuleTargetPageReqVO ruleTargetPageReqVO) {
        return selectPage(ruleTargetPageReqVO, new LambdaQueryWrapperX<RuleTargetDO>()
                .eqIfPresent(RuleTargetDO::getChannel, ruleTargetPageReqVO.getChannel())
                .eqIfPresent(RuleTargetDO::getTargetCode, ruleTargetPageReqVO.getTargetCode())
                .likeIfPresent(RuleTargetDO::getTargetName, ruleTargetPageReqVO.getTargetName())
                .likeIfPresent(RuleTargetDO::getTargetDesc, ruleTargetPageReqVO.getTargetDesc())
                .orderByDesc(RuleTargetDO::getId)
        );
    }

    default RuleTargetDO selectOneByTargetCode(String keyCode) {
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
