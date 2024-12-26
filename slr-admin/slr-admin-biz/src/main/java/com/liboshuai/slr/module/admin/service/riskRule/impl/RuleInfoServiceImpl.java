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
        // 设置模型信息
        setRuleModel(ruleInfoRespVO);
        // 设置条件组
        setRuleCondGroup(ruleCode, ruleInfoRespVO);
        return ruleInfoRespVO;
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
        if (!CollectionUtils.isEmpty(ruleCondDOList)) {
            List<RuleCondRespVO> ruleCondRespVOList = BeanUtils.toBean(ruleCondDOList, RuleCondRespVO.class);
            List<String> eventCodeList = ruleCondRespVOList.stream().map(RuleCondRespVO::getEventCode).collect(Collectors.toList());
            List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByEventCodes(eventCodeList);
            if (!CollectionUtils.isEmpty(ruleEventDOList)) {
                List<RuleEventRespVO> ruleEventRespVOList = BeanUtils.toBean(ruleEventDOList, RuleEventRespVO.class);
                List<String> eventCodes = ruleEventRespVOList.stream().map(RuleEventRespVO::getEventCode).collect(Collectors.toList());
                List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByEventCodes(eventCodes);
                if (!CollectionUtils.isEmpty(ruleEventAttrDOList)) {
                    List<RuleEventAttrRespVO> ruleEventAttrRespVOList = BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrRespVO.class);
                    Map<String, List<RuleEventAttrRespVO>> eventCodeAndRuleEventAttrRespVoMap = ruleEventAttrRespVOList.stream()
                            .collect(Collectors.groupingBy(RuleEventAttrRespVO::getEventCode));
                    // 设置事件属性到事件信息
                    ruleEventRespVOList = ruleEventRespVOList.stream()
                            .map(
                                    ruleEventRespVO ->
                                            ruleEventRespVO.setRuleEventAttributeGroup(
                                                    eventCodeAndRuleEventAttrRespVoMap.get(ruleEventRespVO.getEventCode())
                                            )
                            )
                            .collect(Collectors.toList());
                }
                Map<String, List<RuleEventRespVO>> eventCodeAndRuleEventRespVoMap = ruleEventRespVOList.stream()
                        .collect(Collectors.groupingBy(RuleEventRespVO::getEventCode));
                // 设置事件信息到条件组
                ruleCondRespVOList = ruleCondRespVOList.stream()
                        .map(ruleCondRespVO ->
                                ruleCondRespVO.setRuleEventRespVO(
                                        eventCodeAndRuleEventRespVoMap.get(ruleCondRespVO.getEventCode()).get(0)
                                )
                        ).collect(Collectors.toList());
                // 设置条件组到规则信息
                ruleInfoRespVO.setRuleCondGroup(ruleCondRespVOList);
            }
        }
    }
}
