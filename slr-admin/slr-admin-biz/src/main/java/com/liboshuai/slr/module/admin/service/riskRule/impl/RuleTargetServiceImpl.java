package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.constants.CacheKeyConstants;
import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.framework.redis.core.manager.MultilevelCache;
import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleEventAttrDTO;
import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleEventDTO;
import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleTargetDTO;
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
import java.util.*;
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
    @Resource
    private MultilevelCache multilevelCache;

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
        // 更新缓存
        putCacheDetailList();
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
        // 更新缓存
        putCacheDetailList();
    }

    @Override
    public List<RuleTargetRespVO> list(String channel) {
        List<RuleTargetDO> ruleTargetDOList = ruleTargetMapper.selectListByChannel(channel);
        return BeanUtils.toBean(ruleTargetDOList, RuleTargetRespVO.class);
    }

    @Override
    public List<RuleTargetDTO> putCacheDetailList() {
        List<RuleTargetDO> ruleTargetDOList = ruleTargetMapper.selectList();
        if (CollectionUtils.isEmpty(ruleTargetDOList)) {
            // 放入空数组，防止缓存击穿
            multilevelCache.put(CacheKeyConstants.RULE_TARGET_DETAIL_LIST, new ArrayList<>());
        }
        List<RuleTargetDTO> ruleTargetDTOList = BeanUtils.toBean(ruleTargetDOList, RuleTargetDTO.class);
        // 设置 规则事件组
        ruleTargetDTOList.forEach(this::detailSetRuleEventList);
        multilevelCache.put(CacheKeyConstants.RULE_TARGET_DETAIL_LIST, ruleTargetDTOList);
        return ruleTargetDTOList;
    }

    @Override
    public List<RuleTargetDTO> getCacheDetailList() {
        List<RuleTargetDTO> list = multilevelCache.get(CacheKeyConstants.RULE_TARGET_DETAIL_LIST, List.class);
        // 只需要判断是否为null，不要判断list元素是否为空，防止缓存击穿
        if (Objects.nonNull(list)) {
            return list;
        }
        return putCacheDetailList();
    }

    @Override
    public void refreshCache() {
        this.putCacheDetailList();
    }

    /**
     * 设置 规则事件组
     */
    private void detailSetRuleEventList(RuleTargetRespVO ruletargetRespVO) {
        String targetCode = ruletargetRespVO.getTargetCode();
        if (!StringUtils.hasText(targetCode)) {
            return;
        }
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByTargetCode(targetCode);
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

    /**
     * 设置 规则事件组
     */
    private void detailSetRuleEventList(RuleTargetDTO ruleTargetDTO) {
        String targetCode = ruleTargetDTO.getTargetCode();
        if (!StringUtils.hasText(targetCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_TARGET_NOT_EXISTS);
        }
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByTargetCodeAndStatus(targetCode,
                Arrays.asList(CommonStatusEnum.ONLINE.getCode(), CommonStatusEnum.OFFLINE_PENDING.getCode()));
        if (CollectionUtils.isEmpty(ruleEventDOList)) {
            return;
        }
        List<RuleEventDTO> ruleEventDTOList = BeanUtils.toBean(ruleEventDOList, RuleEventDTO.class);
        List<String> ruleEventCodeList = ruleEventDTOList.stream().map(RuleEventDTO::getEventCode).collect(Collectors.toList());
        // 查询 事件属性组
        List<RuleEventAttrDO> ruleEventAttrDOList = ruleEventAttrMapper.selectListByEventCodes(ruleEventCodeList);
        if (CollectionUtils.isEmpty(ruleEventAttrDOList)) {
            ruleTargetDTO.setRuleEventGroup(ruleEventDTOList);
            return;
        }
        List<RuleEventAttrDTO> ruleEventAttrDTOList = BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrDTO.class);
        Map<String, List<RuleEventAttrDTO>> eventCodeAndEventAttrDtoMap = ruleEventAttrDTOList.stream()
                .collect(Collectors.groupingBy(RuleEventAttrDTO::getEventCode));
        // 给 事件 设置 属性
        ruleEventDTOList.forEach(
                ruleEventDTO ->
                        ruleEventDTO.setRuleEventAttrGroup(eventCodeAndEventAttrDtoMap.get(ruleEventDTO.getEventCode()))
        );
        // 给 目标 设置 事件
        ruleTargetDTO.setRuleEventGroup(ruleEventDTOList);
    }

}
