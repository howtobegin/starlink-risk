package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.dao.mysql.riskRule.RuleInfoMapper;
import com.liboshuai.slr.module.admin.pojo.entity.riskRule.RuleInfoEntity;
import com.liboshuai.slr.module.admin.pojo.vo.riskRule.RuleInfoReqVO;
import com.liboshuai.slr.module.admin.pojo.vo.riskRule.RuleInfoRespVO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RuleInfoServiceImpl implements RuleInfoService {
    @Resource
    private RuleInfoMapper ruleInfoMapper;

    @Override
    public PageResult<RuleInfoRespVO> list(RuleInfoReqVO ruleInfoReqVO) {
        PageResult<RuleInfoEntity> ruleInfoEntityPageResult = ruleInfoMapper.selectPage(ruleInfoReqVO);
        return BeanUtils.toBean(ruleInfoEntityPageResult, RuleInfoRespVO.class);
    }
}
