package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.constants.ErrorCodeConstants;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleEventAttrSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleEventSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleTargetPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleTargetSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventAttrRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleTargetRespVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventAttrDO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventDO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleTargetDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleEventAttrMapper;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleEventMapper;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleTargetMapper;
import com.liboshuai.slr.module.admin.service.riskRule.RuleTargetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
public class RuleTargetServiceImpl implements RuleTargetService {

    @Resource
    private RuleTargetMapper ruleTargetMapper;
    @Resource
    private RuleEventMapper ruleEventMapper;
    @Resource
    private RuleEventAttrMapper ruleEventAttrMapper;

    @Override
    public PageResult<RuleTargetRespVO> list(RuleTargetPageReqVO ruleTargetPageReqVO) {
        PageResult<RuleTargetDO> ruleInfoEntityPageResult = ruleTargetMapper.selectPage(ruleTargetPageReqVO);
        return BeanUtils.toBean(ruleInfoEntityPageResult, RuleTargetRespVO.class);
    }

    @Override
    public RuleTargetRespVO detail(String keyCode) {
        RuleTargetDO ruleTargetDO = ruleTargetMapper.selectOneByTargetCode(keyCode);
        if (Objects.isNull(ruleTargetDO)) {
            return new RuleTargetRespVO();
        }
        RuleTargetRespVO ruletargetRespVO = BeanUtils.toBean(ruleTargetDO, RuleTargetRespVO.class);
        // 设置 规则事件组
        detailSetRuleEventList(ruletargetRespVO);
        return ruletargetRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(RuleTargetSaveReqVO ruleTargetSaveReqVO) {
        // 保存 规则目标信息
        RuleTargetDO ruleTargetDO = BeanUtils.toBean(ruleTargetSaveReqVO, RuleTargetDO.class);
        String targetCode = ruleTargetDO.getTargetCode();
        if (Objects.nonNull(ruleTargetMapper.selectOneByTargetCode(targetCode))) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_TARGET_CODE_EXISTS, targetCode);
        }
        ruleTargetMapper.insert(ruleTargetDO);
        // 保存 规则事件信息
        List<RuleEventSaveReqVO> ruleEventSaveGroup = ruleTargetSaveReqVO.getRuleEventGroup();
        List<RuleEventDO> ruleEventDOList = BeanUtils.toBean(ruleEventSaveGroup, RuleEventDO.class);
        assert ruleEventDOList != null;
        ruleEventDOList.forEach(ruleEventDO -> {
            String eventCode = ruleEventDO.getEventCode();
            if (Objects.nonNull(ruleEventMapper.selectOneByEventCode(eventCode))) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_CODE_EXISTS, eventCode);
            }
        });
        ruleEventMapper.insertBatch(ruleEventDOList);
        // 保存 规则事件属性信息
        List<RuleEventAttrSaveReqVO> ruleEventAttrSaveGroup = ruleEventSaveGroup.stream()
                .filter(ruleEventSaveReqVO -> !CollectionUtils.isEmpty(ruleEventSaveReqVO.getRuleEventAttrGroup()))
                .flatMap(ruleEventSaveReqVO -> ruleEventSaveReqVO.getRuleEventAttrGroup().stream())
                .collect(Collectors.toList());
        List<RuleEventAttrDO> ruleEventAttrDOList = BeanUtils.toBean(ruleEventAttrSaveGroup, RuleEventAttrDO.class);
        ruleEventAttrMapper.insertBatch(ruleEventAttrDOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RuleTargetSaveReqVO ruleTargetSaveReqVO) {
        // 更新 规则目标信息
        String targetCode = ruleTargetSaveReqVO.getTargetCode();
        RuleTargetDO ruleTargetDO = BeanUtils.toBean(ruleTargetSaveReqVO, RuleTargetDO.class);
        ruleTargetMapper.updateByTargetCode(ruleTargetDO, targetCode);
        // 更新 规则事件信息
        List<RuleEventSaveReqVO> ruleEventSaveReqVOList = ruleTargetSaveReqVO.getRuleEventGroup();
        if (CollectionUtils.isEmpty(ruleEventSaveReqVOList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_NOT_NULL);
        }
        List<RuleEventDO> oldRuleEventDOList = ruleEventMapper.selectListByKeyCode(targetCode);
        List<String> oldRuleEventCodeList = oldRuleEventDOList.stream().map(RuleEventDO::getEventCode).collect(Collectors.toList());
        ruleEventMapper.deleteByKeyCode(targetCode);
        ruleEventMapper.insertBatch(BeanUtils.toBean(ruleEventSaveReqVOList, RuleEventDO.class));
        // 更新 规则事件属性信息
        List<RuleEventAttrSaveReqVO> ruleEventAttrGroup = ruleEventSaveReqVOList.stream()
                .filter(ruleEventSaveReqVO -> !CollectionUtils.isEmpty(ruleEventSaveReqVO.getRuleEventAttrGroup()))
                .flatMap(ruleEventSaveReqVO -> ruleEventSaveReqVO.getRuleEventAttrGroup().stream())
                .collect(Collectors.toList());
        ruleEventAttrMapper.deleteByEventCodes(oldRuleEventCodeList);
        ruleEventAttrMapper.insertBatch(BeanUtils.toBean(ruleEventAttrGroup, RuleEventAttrDO.class));
    }

    /**
     * 设置 规则事件组
     */
    private void detailSetRuleEventList(RuleTargetRespVO ruletargetRespVO) {
        String keyCode = ruletargetRespVO.getTargetCode();
        if (!StringUtils.hasText(keyCode)) {
            return;
        }
        // 查询 规则事件组
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByKeyCode(keyCode);
        if (CollectionUtils.isEmpty(ruleEventDOList)) {
            return;
        }
        List<RuleEventRespVO> ruleEventRespVOS = BeanUtils.toBean(ruleEventDOList, RuleEventRespVO.class);
        List<String> ruleEventCodeList = ruleEventRespVOS.stream().map(RuleEventRespVO::getEventCode).collect(Collectors.toList());
        // 查询 事件属性组
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByEventCodes(ruleEventCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrDOList)) {
            ruletargetRespVO.setRuleEventGroup(ruleEventRespVOS);
            return;
        }
        List<RuleEventAttrRespVO> ruleEventAttrRespVOList = BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrRespVO.class);
        Map<String, List<RuleEventAttrRespVO>> eventCodeAndEventAttrRespVoMap = ruleEventAttrRespVOList.stream()
                .collect(Collectors.groupingBy(RuleEventAttrRespVO::getEventCode));
        // 给 事件 设置 属性
        ruleEventRespVOS.forEach(
                ruleEventRespVO ->
                        ruleEventRespVO.setRuleEventAttrGroup(eventCodeAndEventAttrRespVoMap.get(ruleEventRespVO.getEventCode()))
        );
        // 给 目标 设置 事件
        ruletargetRespVO.setRuleEventGroup(ruleEventRespVOS);
    }


}
