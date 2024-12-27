package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.*;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.*;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.*;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RuleInfoServiceImpl implements RuleInfoService {
    @Resource
    private RuleInfoMapper ruleInfoMapper;
    @Resource
    private RuleModelMapper ruleModelMapper;
    @Resource
    private RuleCondMapper ruleCondMapper;
    @Resource
    private RuleEventMapper ruleEventMapper;
    @Resource
    private RuleEventAttrMapper ruleEventAttrMapper;
    @Resource
    private RuleEventAttrValueMapper ruleEventAttrValueMapper;
    @Resource
    private RuleKeyMapper ruleKeyMapper;

    @Override
    public PageResult<RuleInfoRespVO> list(RuleInfoReqVO ruleInfoReqVO) {
        PageResult<RuleInfoDO> ruleInfoEntityPageResult = ruleInfoMapper.selectPage(ruleInfoReqVO);
        return BeanUtils.toBean(ruleInfoEntityPageResult, RuleInfoRespVO.class);
    }

    @Override
    public RuleInfoRespVO detail(String ruleCode) {
        RuleInfoDO ruleInfoDO = ruleInfoMapper.selectOneByRuleCode(ruleCode);
        if (Objects.isNull(ruleInfoDO)) {
            return new RuleInfoRespVO();
        }
        RuleInfoRespVO ruleInfoRespVO = BeanUtils.toBean(ruleInfoDO, RuleInfoRespVO.class);
        // 设置目标信息
        setRuleKey(ruleInfoRespVO);
        // 设置模型信息
        setRuleModel(ruleInfoRespVO);
        // 设置条件组
        setRuleCondGroup(ruleCode, ruleInfoRespVO);
        return ruleInfoRespVO;
    }

    /**
     * 设置目标信息
     */
    private void setRuleKey(RuleInfoRespVO ruleInfoRespVO) {
        String keyCode = ruleInfoRespVO.getKeyCode();
        if (!StringUtils.hasText(keyCode)) {
            return;
        }
        RuleKeyDO ruleKeyDO = ruleKeyMapper.selectOneByKeyCode(keyCode);
        if (Objects.isNull(ruleKeyDO)) {
            return;
        }
        RuleKeyRespVO ruleKeyRespVO = BeanUtils.toBean(ruleKeyDO, RuleKeyRespVO.class);
        ruleInfoRespVO.setRuleKeyRespVO(ruleKeyRespVO);
    }

    /**
     * 设置模型信息
     */
    private void setRuleModel(RuleInfoRespVO ruleInfoRespVO) {
        String modelCode = ruleInfoRespVO.getModelCode();
        if (StringUtils.hasText(modelCode)) {
            RuleModelDO ruleModelDO = ruleModelMapper.selectOneByModelCode(modelCode);
            RuleModelRespVO ruleModelRespVO = BeanUtils.toBean(ruleModelDO, RuleModelRespVO.class);
            ruleInfoRespVO.setRuleModelRespVO(ruleModelRespVO);
        }
    }

    /**
     * 设置条件组
     */
    private void setRuleCondGroup(String ruleCode, RuleInfoRespVO ruleInfoRespVO) {
        List<RuleCondDO> ruleCondDOList = ruleCondMapper.selectListByModelCode(ruleCode);
        if (CollectionUtils.isEmpty(ruleCondDOList)) {
            return;
        }
        List<RuleCondRespVO> ruleCondRespVOList = BeanUtils.toBean(ruleCondDOList, RuleCondRespVO.class);
        List<String> eventCodeList = ruleCondRespVOList.stream().map(RuleCondRespVO::getEventCode).collect(Collectors.toList());
        List<String> condCodeList = ruleCondRespVOList.stream().map(RuleCondRespVO::getCondCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(eventCodeList) || CollectionUtils.isEmpty(condCodeList)) {
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByEventCodes(eventCodeList);
        if (CollectionUtils.isEmpty(ruleEventDOList)) {
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventRespVO> ruleEventRespVOList = BeanUtils.toBean(ruleEventDOList, RuleEventRespVO.class);
        Map<String, List<RuleEventRespVO>> envetCodeAndRuleEventRespVoMap = ruleEventRespVOList.stream()
                .collect(Collectors.groupingBy(RuleEventRespVO::getEventCode));
        List<RuleEventAttrValueDO> ruleEventAttrValueDOList = ruleEventAttrValueMapper.selectListByCondCodes(condCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrValueDOList)) {
            ruleCondRespVOList = ruleCondRespVOList.stream().map(ruleCondRespVO -> ruleCondRespVO.setRuleEventRespVO(envetCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode()).get(0))).collect(Collectors.toList());
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventAttrValueRespVO> ruleEventAttrValueRespVOList = BeanUtils.toBean(ruleEventAttrValueDOList, RuleEventAttrValueRespVO.class);
        Map<String, List<RuleEventAttrValueRespVO>> codeAndAttrValueRespVoMap = ruleEventAttrValueRespVOList.stream().collect(Collectors.groupingBy(RuleEventAttrValueRespVO::getAttributeCode));
        List<String> attributeCodeList = ruleEventAttrValueRespVOList.stream().map(RuleEventAttrValueRespVO::getAttributeCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attributeCodeList)) {
            ruleCondRespVOList = ruleCondRespVOList.stream().map(ruleCondRespVO -> ruleCondRespVO.setRuleEventRespVO(envetCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode()).get(0))).collect(Collectors.toList());
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByAttributeCodes(attributeCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrDOList)) {
            ruleCondRespVOList = ruleCondRespVOList.stream().map(ruleCondRespVO -> ruleCondRespVO.setRuleEventRespVO(envetCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode()).get(0))).collect(Collectors.toList());
            ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
            return;
        }
        List<RuleEventAttrRespVO> ruleEventAttrRespVOList = BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrRespVO.class);
        ruleEventAttrRespVOList = ruleEventAttrRespVOList.stream().map(ruleEventAttrRespVO -> ruleEventAttrRespVO.setRuleEventAttrValueRespVO(codeAndAttrValueRespVoMap.get(ruleEventAttrRespVO.getAttributeCode()).get(0))).collect(Collectors.toList());
        Map<String, List<RuleEventAttrRespVO>> eventCodeAndEventAttrRespVoMap = ruleEventAttrRespVOList.stream().collect(Collectors.groupingBy(RuleEventAttrRespVO::getEventCode));
        ruleEventRespVOList = ruleEventRespVOList.stream().map(ruleEventRespVO -> ruleEventRespVO.setRuleEventAttrRespVoList(eventCodeAndEventAttrRespVoMap.get(ruleEventRespVO.getEventCode()))).collect(Collectors.toList());
        Map<String, List<RuleEventRespVO>> eventCodeAndEventRespVoMap = ruleEventRespVOList.stream().collect(Collectors.groupingBy(RuleEventRespVO::getEventCode));
        ruleCondRespVOList = ruleCondRespVOList.stream().map(ruleCondRespVO -> ruleCondRespVO.setRuleEventRespVO(eventCodeAndEventRespVoMap.get(ruleCondRespVO.getEventCode()).get(0))).collect(Collectors.toList());
        ruleInfoRespVO.setRuleCondRespVoList(ruleCondRespVOList);
    }
}
