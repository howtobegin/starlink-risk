package com.liboshuai.starlink.slr.admin.service.riskRule.impl;

import com.liboshuai.starlink.slr.admin.dao.mysql.riskRule.RuleInfoMapper;
import com.liboshuai.starlink.slr.admin.pojo.entity.riskRule.RuleInfoEntity;
import com.liboshuai.starlink.slr.admin.pojo.vo.riskRule.RuleInfoReqVO;
import com.liboshuai.starlink.slr.admin.pojo.vo.riskRule.RuleInfoRespVO;
import com.liboshuai.starlink.slr.admin.service.riskRule.RuleInfoService;
import com.liboshuai.starlink.slr.framework.common.pojo.PageResult;
import com.liboshuai.starlink.slr.framework.common.util.object.BeanUtils;
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
