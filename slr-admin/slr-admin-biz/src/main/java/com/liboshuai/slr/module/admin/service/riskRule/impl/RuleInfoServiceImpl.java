package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleInfoReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleInfoRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleModelRespVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleInfoDO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleModelDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleInfoMapper;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleModelMapper;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RuleInfoServiceImpl implements RuleInfoService {
    @Resource
    private RuleInfoMapper ruleInfoMapper;
    @Resource
    private RuleModelMapper ruleModelMapper;

    @Override
    public PageResult<RuleInfoRespVO> list(RuleInfoReqVO ruleInfoReqVO) {
        PageResult<RuleInfoDO> ruleInfoEntityPageResult = ruleInfoMapper.selectPage(ruleInfoReqVO);
        PageResult<RuleInfoRespVO> ruleInfoRespVOPageResult = BeanUtils.toBean(ruleInfoEntityPageResult, RuleInfoRespVO.class);
        List<RuleInfoRespVO> ruleInfoRespVOList = ruleInfoRespVOPageResult.getList();
        if (CollectionUtils.isEmpty(ruleInfoRespVOList)) {
            return ruleInfoRespVOPageResult;
        }
        // 设置模型信息
        PageResult<RuleInfoRespVO> respVOPageResult = setRuleModelRespVO(ruleInfoRespVOList, ruleInfoRespVOPageResult);
        if (respVOPageResult != null) return respVOPageResult;
        // 设置条件组

        return ruleInfoRespVOPageResult;
    }

    private PageResult<RuleInfoRespVO> setRuleModelRespVO(List<RuleInfoRespVO> ruleInfoRespVOList, PageResult<RuleInfoRespVO> ruleInfoRespVOPageResult) {
        List<String> modelCodeList = ruleInfoRespVOList.stream().map(RuleInfoRespVO::getModelCode).collect(Collectors.toList());
        List<RuleModelDO> ruleModelDOList = ruleModelMapper.selectListByModelCode(modelCodeList);
        if (CollectionUtils.isEmpty(ruleModelDOList)) {
            return ruleInfoRespVOPageResult;
        }
        List<RuleModelRespVO> ruleModelRespVOList = BeanUtils.toBean(ruleModelDOList, RuleModelRespVO.class);
        Map<String, List<RuleModelRespVO>> ruleCodeAndRuleModelRespVoMap = ruleModelRespVOList.stream()
                .collect(Collectors.groupingBy(RuleModelRespVO::getModelCode));
        List<RuleInfoRespVO> ruleInfoRespVOS = ruleInfoRespVOList.stream().map(ruleInfoRespVO -> {
            List<RuleModelRespVO> ruleModelRespVOS = ruleCodeAndRuleModelRespVoMap.get(ruleInfoRespVO.getModelCode());
            if (CollectionUtils.isEmpty(ruleModelRespVOS)) {
                return ruleInfoRespVO;
            }
            ruleInfoRespVO.setRuleModelRespVO(ruleModelRespVOS.get(0));
            return ruleInfoRespVO;
        }).collect(Collectors.toList());
        ruleInfoRespVOPageResult.setList(ruleInfoRespVOS);
        return null;
    }
}
