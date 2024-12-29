package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.constants.ErrorCodeConstants;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.*;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventAttrRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleTargetRespVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventAttrDO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventDO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleTargetDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleEventAttrMapper;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleEventMapper;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleTargetMapper;
import com.liboshuai.slr.module.admin.framework.component.snowflake.SnowflakeIdGenerator;
import com.liboshuai.slr.module.admin.service.riskRule.RuleTargetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.liboshuai.slr.module.engine.constants.SnowflakeIdPrefixConstants.ATTR_CODE_PREFIX;

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
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public PageResult<RuleTargetRespVO> list(RuleTargetPageReqVO ruleTargetPageReqVO) {
        PageResult<RuleTargetDO> ruleInfoEntityPageResult = ruleTargetMapper.selectPage(ruleTargetPageReqVO);
        return BeanUtils.toBean(ruleInfoEntityPageResult, RuleTargetRespVO.class);
    }

    @Override
    public RuleTargetRespVO detail(String keyCode) {
        RuleTargetDO ruleTargetDO = ruleTargetMapper.selectOneByKeyCode(keyCode);
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
        String ruleKeyCode = ruleTargetDO.getTargetCode();
        if (Objects.nonNull(ruleTargetMapper.selectOneByKeyCode(ruleKeyCode))) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_KEY_CODE_EXISTS, ruleKeyCode);
        }
        ruleTargetMapper.insert(ruleTargetDO);
        // 保存 规则事件信息
        List<RuleEventSaveReqVO> ruleEventSaveReqVOList = ruleTargetSaveReqVO.getRuleEventSaveReqVOList();
        List<RuleEventDO> ruleEventDOList = BeanUtils.toBean(ruleEventSaveReqVOList, RuleEventDO.class);
        assert ruleEventDOList != null;
        ruleEventDOList.forEach(ruleEventDO -> {
            String eventCode = ruleEventDO.getEventCode();
            if (Objects.nonNull(ruleEventMapper.selectOneByEventCode(eventCode))) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_CODE_EXISTS, eventCode);
            }
        });
        ruleEventMapper.insertBatch(ruleEventDOList);
        // 保存 规则事件属性信息
        List<RuleEventAttrDO> ruleEventAttrDOList = new ArrayList<>();
        for (RuleEventSaveReqVO ruleEventSaveReqVO : ruleEventSaveReqVOList) {
            List<RuleEventAttrSaveRespVO> ruleEventAttrSaveRespVOList = ruleEventSaveReqVO.getRuleEventAttrGroup();
            if (CollectionUtils.isEmpty(ruleEventAttrSaveRespVOList)) {
                continue;
            }
            ruleEventAttrSaveRespVOList.forEach(
                    ruleEventAttrSaveRespVO ->
                            ruleEventAttrSaveRespVO.setAttributeCode(ATTR_CODE_PREFIX + snowflakeIdGenerator.nextIdStr())
            );
            ruleEventAttrDOList.addAll(BeanUtils.toBean(ruleEventAttrSaveRespVOList, RuleEventAttrDO.class));
        }
        ruleEventAttrMapper.insertBatch(ruleEventAttrDOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RuleTargetSaveReqVO ruleTargetSaveReqVO) {
        // 更新 规则目标信息
        validateUniqueRuleKey(ruleTargetSaveReqVO.getId(), ruleTargetSaveReqVO.getTargetCode()); // 效验 ruleKey 唯一
        RuleTargetDO oldRuleTargetDO = ruleTargetMapper.selectById(ruleTargetSaveReqVO.getId());
        ruleTargetMapper.updateById(BeanUtils.toBean(ruleTargetSaveReqVO, RuleTargetDO.class));
        // 更新 规则事件信息
        List<RuleEventSaveReqVO> ruleEventSaveReqVOList = ruleTargetSaveReqVO.getRuleEventSaveReqVOList();
        batchValidateUniqueEventCode(ruleEventSaveReqVOList); // 批量效验 eventCode 唯一
        List<RuleEventDO> oldRuleEventDOList = ruleEventMapper.selectListByKeyCode(oldRuleTargetDO.getTargetCode());
        List<String> oldRuleEventCodeList = oldRuleEventDOList.stream().map(RuleEventDO::getEventCode).collect(Collectors.toList());
        ruleEventMapper.deleteByKeyCode(oldRuleTargetDO.getTargetCode());
        ruleEventMapper.insertBatch(BeanUtils.toBean(ruleEventSaveReqVOList, RuleEventDO.class));
        // 更新 规则事件属性信息
        List<RuleEventAttrDO> ruleEventAttrDOList = new ArrayList<>();
        for (RuleEventSaveReqVO ruleEventSaveReqVO : ruleEventSaveReqVOList) {
            List<RuleEventAttrSaveRespVO> ruleEventAttrSaveRespVOList = ruleEventSaveReqVO.getRuleEventAttrGroup();
            if (CollectionUtils.isEmpty(ruleEventAttrSaveRespVOList)) {
                continue;
            }
            ruleEventAttrSaveRespVOList = ruleEventAttrSaveRespVOList.stream()
                    .filter(ruleEventAttrSaveRespVO -> !StringUtils.hasText(ruleEventAttrSaveRespVO.getAttributeCode()))
                    .peek(ruleEventAttrSaveRespVO -> ruleEventAttrSaveRespVO.setAttributeCode(ATTR_CODE_PREFIX + snowflakeIdGenerator.nextIdStr()))
                    .collect(Collectors.toList());
            ruleEventAttrDOList.addAll(BeanUtils.toBean(ruleEventAttrSaveRespVOList, RuleEventAttrDO.class));
        }
        ruleEventAttrMapper.deleteByEventCodes(oldRuleEventCodeList);
        ruleEventAttrMapper.insertBatch(ruleEventAttrDOList);
    }

    @Override
    public boolean checkUniqueKeyCode(CheckUniqueTargetCodeReqVO checkUniqueTargetCodeReqVO) {
        Long keyId = checkUniqueTargetCodeReqVO.getTargetId();
        String keyCode = checkUniqueTargetCodeReqVO.getTargetCode();
        List<RuleTargetDO> ruleTargetDOList;
        if (Objects.isNull(keyId)) {
            ruleTargetDOList = ruleTargetMapper.selectList();
        } else {
            RuleTargetDO ruleTargetDO = ruleTargetMapper.selectById(keyId);
            if (Objects.isNull(ruleTargetDO)) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_KEY_ID_NOT_EXISTS, keyId);
            }
            ruleTargetDOList = ruleTargetMapper.selectOneByNeId(keyId);
        }
        if (!CollectionUtils.isEmpty(ruleTargetDOList)) {
            for (RuleTargetDO keyDO : ruleTargetDOList) {
                if (Objects.equals(keyDO.getTargetCode(), keyCode)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean checkUniqueEventCode(CheckUniqueEventCodeReqVO checkUniqueEventCodeReqVO) {
        Long eventId = checkUniqueEventCodeReqVO.getEventId();
        String eventCode = checkUniqueEventCodeReqVO.getEventCode();
        List<RuleEventDO> ruleEventDOList;
        if (Objects.isNull(eventId)) {
            ruleEventDOList = ruleEventMapper.selectList();
        } else {
            RuleEventDO ruleEventDO = ruleEventMapper.selectById(eventId);
            if (Objects.isNull(ruleEventDO)) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_ID_NOT_EXISTS, eventId);
            }
            ruleEventDOList = ruleEventMapper.selectListByNeId(eventId);
        }
        if (!CollectionUtils.isEmpty(ruleEventDOList)) {
            List<String> ruleEventCodeList = ruleEventDOList.stream()
                    .map(RuleEventDO::getEventCode)
                    .collect(Collectors.toList());
            return !ruleEventCodeList.contains(eventCode);
        }
        return true;
    }

    /**
     * 批量效验 eventCode 唯一
     */
    private void batchValidateUniqueEventCode(List<RuleEventSaveReqVO> ruleEventSaveReqVOList) {
        if (CollectionUtils.isEmpty(ruleEventSaveReqVOList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_NOT_NULL);
        }
        List<String> ruleEventCodeList = ruleEventSaveReqVOList.stream().map(RuleEventSaveReqVO::getEventCode)
                .collect(Collectors.toList());
        List<Long> ruleEventIdList = new ArrayList<>();
        for (RuleEventSaveReqVO ruleEventSaveReqVO : ruleEventSaveReqVOList) {
            Long id = ruleEventSaveReqVO.getId();
            if (Objects.isNull(id)) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_ID_NOT_NULL);
            }
            ruleEventIdList.add(id);
        }
        List<RuleEventDO> ruleEventDOS = ruleEventMapper.selectListByNotInIds(ruleEventIdList);
        if (!CollectionUtils.isEmpty(ruleEventDOS)) {
            List<String> eventCodeList = ruleEventDOS.stream().map(RuleEventDO::getEventCode).collect(Collectors.toList());
            eventCodeList.retainAll(ruleEventCodeList);
            if (!CollectionUtils.isEmpty(eventCodeList)) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_CODE_EXISTS, JsonUtils.toJsonString(eventCodeList));
            }
        }
    }

    /**
     * 效验 ruleKey 唯一
     */
    private void validateUniqueRuleKey(Long keyId, String keyCode) {
        if (Objects.isNull(keyId)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_KEY_ID_NOT_NULL);
        }
        List<RuleTargetDO> ruleTargetDOList = ruleTargetMapper.selectOneByNeId(keyId);
        if (!CollectionUtils.isEmpty(ruleTargetDOList)) {
            for (RuleTargetDO keyDO : ruleTargetDOList) {
                if (Objects.equals(keyDO.getTargetCode(), keyCode)) {
                    throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_KEY_CODE_EXISTS, keyCode);
                }
            }
        }
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
