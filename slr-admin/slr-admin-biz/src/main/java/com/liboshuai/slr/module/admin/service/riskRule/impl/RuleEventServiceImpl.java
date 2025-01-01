package com.liboshuai.slr.module.admin.service.riskRule.impl;

import com.liboshuai.slr.framework.common.enums.CommonAuditOpEnum;
import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.common.exception.util.ServiceExceptionUtil;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.constants.ErrorCodeConstants;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleEventChangeStatusReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventRespVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleEventDO;
import com.liboshuai.slr.module.admin.dal.mysql.riskRule.RuleEventMapper;
import com.liboshuai.slr.module.admin.service.riskRule.RuleEventService;
import com.liboshuai.slr.module.admin.service.riskRule.RuleTargetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class RuleEventServiceImpl implements RuleEventService {

    @Resource
    private RuleEventMapper ruleEventMapper;
    @Resource
    private RuleTargetService ruleTargetService;

    @Override
    public void changeStatus(RuleEventChangeStatusReqVO ruleEventChangeStatusReqVO) {
        String eventCode = ruleEventChangeStatusReqVO.getEventCode();
        String auditOp = ruleEventChangeStatusReqVO.getAuditOp();
        RuleEventDO ruleEventDO = ruleEventMapper.selectOneByEventCode(eventCode);
        if (Objects.isNull(ruleEventDO)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.RULE_EVENT_NOT_EXISTS, eventCode);
        }
        String eventStatus = ruleEventDO.getEventStatus();
        String newEventStatus = ruleEventChangeStatusReqVO.getNewEventStatus();
        if (Objects.equals(newEventStatus, CommonStatusEnum.ONLINE_PENDING.getCode())) {
            // 进行上线操作
            if (!Objects.equals(eventStatus, CommonStatusEnum.DRAFT.getCode())
                    && !Objects.equals(eventStatus, CommonStatusEnum.OFFLINE.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.STATUS_NOT_DRAFT);
            }
            ruleEventDO.setEventStatus(newEventStatus);
        } else if (Objects.equals(newEventStatus, CommonStatusEnum.ONLINE.getCode())) {
            // 进行上线审核操作
            if (!Objects.equals(eventStatus, CommonStatusEnum.ONLINE_PENDING.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.STATUS_NOT_ONLINE_PENDING);
            }
            if (Objects.equals(auditOp, CommonAuditOpEnum.APPROVE.getCode())) { // 审核通过
                ruleEventDO.setEventStatus(newEventStatus);
            } else if (Objects.equals(auditOp, CommonAuditOpEnum.REJECT.getCode())) { // 审核拒绝
                ruleEventDO.setEventStatus(CommonStatusEnum.DRAFT.getCode());
            } else {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.AUDIT_OP_NOT_SUPPORT);
            }
        } else if (Objects.equals(newEventStatus, CommonStatusEnum.OFFLINE_PENDING.getCode())) {
            // 进行下线操作
            if (!Objects.equals(eventStatus, CommonStatusEnum.ONLINE.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.STATUS_NOT_ONLINE);
            }
            ruleEventDO.setEventStatus(newEventStatus);
        } else if (Objects.equals(newEventStatus, CommonStatusEnum.OFFLINE.getCode())) {
            // 进行下线审核操作
            if (!Objects.equals(eventStatus, CommonStatusEnum.OFFLINE_PENDING.getCode())) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.STATUS_NOT_OFFLINE_PENDING);
            }
            if (Objects.equals(auditOp, CommonAuditOpEnum.APPROVE.getCode())) { // 审核通过
                ruleEventDO.setEventStatus(newEventStatus);
            } else if (Objects.equals(auditOp, CommonAuditOpEnum.REJECT.getCode())) { // 审核拒绝
                ruleEventDO.setEventStatus(CommonStatusEnum.ONLINE.getCode());
            } else {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.AUDIT_OP_NOT_SUPPORT);
            }
        } else {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.NEW_STATUS_NOT_SUPPORT);
        }
        ruleEventMapper.updateByEventCode(ruleEventDO, eventCode);
        // 更新缓存
        ruleTargetService.putCacheDetailList();
    }

    @Override
    public List<RuleEventRespVO> list(String targetCode) {
        List<RuleEventDO> ruleEventDOList = ruleEventMapper.selectListByTargetCode(targetCode);
        return BeanUtils.toBean(ruleEventDOList, RuleEventRespVO.class);
    }
}
