package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.constants.ErrorCodeConstants;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleEventAttrSaveRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleEventSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeyPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeySaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventAttrRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventRespVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleKeyRespVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventAttrDO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventDO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleKeyDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleEventAttrMapper;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleEventMapper;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleKeyMapper;
import com.liboshuai.slr.module.admin.framework.component.snowflake.SnowflakeIdGenerator;
import com.liboshuai.slr.module.admin.service.riskRule.RuleKeyService;
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
public class RuleKeyServiceImpl implements RuleKeyService {

    @Resource
    private RuleKeyMapper ruleKeyMapper;
    @Resource
    private RuleEventMapper ruleEventMapper;
    @Resource
    private RuleEventAttrMapper ruleEventAttrMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public PageResult<RuleKeyRespVO> list(RuleKeyPageReqVO ruleKeyPageReqVO) {
        PageResult<RuleKeyDO> ruleInfoEntityPageResult = ruleKeyMapper.selectPage(ruleKeyPageReqVO);
        return BeanUtils.toBean(ruleInfoEntityPageResult, RuleKeyRespVO.class);
    }

    @Override
    public RuleKeyRespVO detail(String keyCode) {
        RuleKeyDO ruleKeyDO = ruleKeyMapper.selectOneByKeyCode(keyCode);
        if (Objects.isNull(ruleKeyDO)) {
            return new RuleKeyRespVO();
        }
        RuleKeyRespVO ruleKeyRespVO = BeanUtils.toBean(ruleKeyDO, RuleKeyRespVO.class);
        // 设置 规则事件组
        detailSetRuleEventList(ruleKeyRespVO);
        return ruleKeyRespVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(RuleKeySaveReqVO ruleKeySaveReqVO) {
        // 保存 规则目标信息
        RuleKeyDO ruleKeyDO = BeanUtils.toBean(ruleKeySaveReqVO, RuleKeyDO.class);
        String ruleKeyCode = ruleKeyDO.getKeyCode();
        if (Objects.nonNull(ruleKeyMapper.selectOneByKeyCode(ruleKeyCode))) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_KEY_CODE_EXISTS, ruleKeyCode);
        }
        ruleKeyMapper.insert(ruleKeyDO);
        // 保存 规则事件信息
        List<RuleEventSaveReqVO> ruleEventSaveReqVOList = ruleKeySaveReqVO.getRuleEventSaveReqVOList();
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
            List<RuleEventAttrSaveRespVO> ruleEventAttrSaveRespVOList = ruleEventSaveReqVO.getRuleEventAttrSaveRespVOList();
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
    public void update(RuleKeySaveReqVO ruleKeySaveReqVO) {
        // 更新 规则目标信息
        validateUniqueRuleKey(ruleKeySaveReqVO.getId(), ruleKeySaveReqVO.getKeyCode()); // 效验 ruleKey 唯一
        RuleKeyDO oldRuleKeyDO = ruleKeyMapper.selectById(ruleKeySaveReqVO.getId());
        ruleKeyMapper.updateById(BeanUtils.toBean(ruleKeySaveReqVO, RuleKeyDO.class));
        // 更新 规则事件信息
        List<RuleEventSaveReqVO> ruleEventSaveReqVOList = ruleKeySaveReqVO.getRuleEventSaveReqVOList();
        batchValidateUniqueEventCode(ruleEventSaveReqVOList); // 批量效验 eventCode 唯一
        List<RuleEventDO> oldRuleEventDOList = ruleEventMapper.selectListByKeyCode(oldRuleKeyDO.getKeyCode());
        List<String> oldRuleEventCodeList = oldRuleEventDOList.stream().map(RuleEventDO::getEventCode).collect(Collectors.toList());
        ruleEventMapper.deleteByKeyCode(oldRuleKeyDO.getKeyCode());
        ruleEventMapper.insertBatch(BeanUtils.toBean(ruleEventSaveReqVOList, RuleEventDO.class));
        // 更新 规则事件属性信息
        List<RuleEventAttrDO> ruleEventAttrDOList = new ArrayList<>();
        for (RuleEventSaveReqVO ruleEventSaveReqVO : ruleEventSaveReqVOList) {
            List<RuleEventAttrSaveRespVO> ruleEventAttrSaveRespVOList = ruleEventSaveReqVO.getRuleEventAttrSaveRespVOList();
            if (CollectionUtils.isEmpty(ruleEventAttrSaveRespVOList)) {
                continue;
            }
            ruleEventAttrDOList.addAll(BeanUtils.toBean(ruleEventAttrSaveRespVOList, RuleEventAttrDO.class));
        }
        ruleEventAttrMapper.deleteByEventCodes(oldRuleEventCodeList);
        ruleEventAttrMapper.insertBatch(ruleEventAttrDOList);
    }

    @Override
    public boolean checkUniqueKeyCode(Long keyId, String keyCode) {
        List<RuleKeyDO> ruleKeyDOList = ruleKeyMapper.selectOneByNeId(keyId);
        if (!CollectionUtils.isEmpty(ruleKeyDOList)) {
            for (RuleKeyDO keyDO : ruleKeyDOList) {
                if (Objects.equals(keyDO.getKeyCode(), keyCode)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean checkUniqueEventCode(Long eventId, String eventCode) {
        RuleEventDO ruleEventDO = ruleEventMapper.selectById(eventId);
        if (Objects.isNull(ruleEventDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_ID_NOT_EXISTS, eventId);
        }
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByNeId(eventId);
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
            eventCodeList.removeAll(ruleEventCodeList);
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
        List<RuleKeyDO> ruleKeyDOList = ruleKeyMapper.selectOneByNeId(keyId);
        if (!CollectionUtils.isEmpty(ruleKeyDOList)) {
            for (RuleKeyDO keyDO : ruleKeyDOList) {
                if (Objects.equals(keyDO.getKeyCode(), keyCode)) {
                    throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_KEY_CODE_EXISTS, keyCode);
                }
            }
        }
    }

    /**
     * 设置 规则事件组
     */
    private void detailSetRuleEventList(RuleKeyRespVO ruleKeyRespVO) {
        String keyCode = ruleKeyRespVO.getKeyCode();
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
            ruleKeyRespVO.setRuleEventRespVOList(ruleEventRespVOS);
            return;
        }
        List<RuleEventAttrRespVO> ruleEventAttrRespVOList = BeanUtils.toBean(ruleEventAttrDOList, RuleEventAttrRespVO.class);
        Map<String, List<RuleEventAttrRespVO>> eventCodeAndEventAttrRespVoMap = ruleEventAttrRespVOList.stream()
                .collect(Collectors.groupingBy(RuleEventAttrRespVO::getEventCode));
        // 设置 事件属性组
        ruleEventRespVOS.forEach(
                ruleEventRespVO ->
                        ruleEventRespVO.setRuleEventAttrRespVoList(eventCodeAndEventAttrRespVoMap.get(ruleEventRespVO.getEventCode()))
        );
        // 设置 规则事件组
        ruleKeyRespVO.setRuleEventRespVOList(ruleEventRespVOS);
    }


}
