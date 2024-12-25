package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleInfoReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleInfoRespVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleInfoDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleInfoMapper;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RuleInfoServiceImpl implements RuleInfoService {
    @Resource
    private RuleInfoMapper ruleInfoMapper;

    @Override
    public PageResult<RuleInfoRespVO> list(RuleInfoReqVO ruleInfoReqVO) {
        PageResult<RuleInfoDO> ruleInfoEntityPageResult = ruleInfoMapper.selectPage(ruleInfoReqVO);
        return BeanUtils.toBean(ruleInfoEntityPageResult, RuleInfoRespVO.class);
    }
}
