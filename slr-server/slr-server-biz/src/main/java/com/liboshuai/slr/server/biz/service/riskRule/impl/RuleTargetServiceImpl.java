package com.liboshuai.slr.server.biz.service.riskRule.impl;

import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.server.api.constants.ErrorCodeConstants;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleEventAttrSaveReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleEventSaveReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleTargetPageReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleTargetSaveReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleEventAttrRespVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleEventRespVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleTargetRespVO;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.RuleEventAttrDO;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.RuleEventDO;
import com.liboshuai.slr.server.biz.dal.dataobject.rule.RuleTargetDO;
import com.liboshuai.slr.server.biz.dal.mysql.rule.RuleEventAttrMapper;
import com.liboshuai.slr.server.biz.dal.mysql.rule.RuleEventMapper;
import com.liboshuai.slr.server.biz.dal.mysql.rule.RuleTargetMapper;
import com.liboshuai.slr.server.biz.service.riskRule.RuleTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class RuleTargetServiceImpl implements RuleTargetService {

    private final RuleTargetMapper ruleTargetMapper;
    private final RuleEventMapper ruleEventMapper;
    private final RuleEventAttrMapper ruleEventAttrMapper;

    @Override
    public PageResult<RuleTargetRespVO> page(RuleTargetPageReqVO ruleTargetPageReqVO) {
        PageResult<RuleTargetDO> ruleInfoEntityPageResult = ruleTargetMapper.selectPage(ruleTargetPageReqVO);
        return BeanUtils.toBean(ruleInfoEntityPageResult, RuleTargetRespVO.class);
    }

    @Override
    public RuleTargetRespVO detail(String targetCode) {
        RuleTargetDO ruleTargetDO = ruleTargetMapper.selectOneByTargetCode(targetCode);
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
            // 设置 事件状态 为草稿
            ruleEventDO.setEventStatus(CommonStatusEnum.DRAFT.getCode());
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
        ruleEventSaveReqVOList.forEach(ruleEventSaveReqVO -> {
            if (Objects.isNull(ruleEventSaveReqVO.getEventStatus())) {
                ruleEventSaveReqVO.setEventStatus(CommonStatusEnum.DRAFT.getCode());
            }
        });
        List<RuleEventDO> oldRuleEventDOList = ruleEventMapper.selectListByTargetCode(targetCode);
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

    @Override
    public List<RuleTargetRespVO> list(String channel) {
        List<RuleTargetDO> ruleTargetDOList = ruleTargetMapper.selectListByChannel(channel);
        return BeanUtils.toBean(ruleTargetDOList, RuleTargetRespVO.class);
    }

    /**
     * 设置 规则事件组
     */
    // TODO: bug修复，查询不出来数据
    private void detailSetRuleEventList(RuleTargetRespVO ruleTargetRespVO) {
        String targetCode = ruleTargetRespVO.getTargetCode();
        if (!StringUtils.hasText(targetCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_TARGET_NOT_EXISTS);
        }
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByTargetCode(targetCode);
        if (CollectionUtils.isEmpty(ruleEventDOList)) {
            return;
        }
        List<RuleEventRespVO> ruleEventRespVOList = BeanUtils.toBean(ruleEventDOList, RuleEventRespVO.class);
        List<String> ruleEventCodeList = ruleEventRespVOList.stream().map(RuleEventRespVO::getEventCode).collect(Collectors.toList());
        // 查询 事件属性组
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByEventCodes(ruleEventCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrDOList)) {
            ruleTargetRespVO.setRuleEventGroup(ruleEventRespVOList);
            return;
        }
        List<RuleEventAttrRespVO> ruleEventAttrRespVOList = BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrRespVO.class);
        Map<String, List<RuleEventAttrRespVO>> eventCodeAndEventAttrDtoMap = ruleEventAttrRespVOList.stream()
                .collect(Collectors.groupingBy(RuleEventAttrRespVO::getEventCode));
        // 给 事件 设置 属性
        ruleEventRespVOList.forEach(
                ruleEventRespVO ->
                        ruleEventRespVO.setRuleEventAttrGroup(eventCodeAndEventAttrDtoMap.get(ruleEventRespVO.getEventCode()))
        );
        // 给 目标 设置 事件
        ruleTargetRespVO.setRuleEventGroup(ruleEventRespVOList);
    }

    @Override
    public Boolean checkUniqueTargetCode(String targetCode) {
        return ruleTargetMapper.selectOneByTargetCode(targetCode) == null;
    }

    @Override
    public List<RuleTargetRespVO> listDetail() {
        List<RuleTargetDO> ruleTargetDOList = ruleTargetMapper.selectList();
        List<RuleTargetRespVO> ruleTargetRespVOList = BeanUtils.toBean(ruleTargetDOList, RuleTargetRespVO.class);
        // 设置 规则事件组
        ruleTargetRespVOList.forEach(this::detailSetRuleEventList);
        return ruleTargetRespVOList;
    }

}
