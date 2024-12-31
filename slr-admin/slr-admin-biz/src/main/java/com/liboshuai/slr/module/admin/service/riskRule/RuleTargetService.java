package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleTargetChangeStatusReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleTargetPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleTargetSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleTargetRespVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public interface RuleTargetService {

    PageResult<RuleTargetRespVO> list(@Valid RuleTargetPageReqVO ruleTargetPageReqVO);

    RuleTargetRespVO detail(@NotBlank String keyCode);

    void create(@Valid RuleTargetSaveReqVO ruleTargetSaveReqVO);

    void update(@Valid RuleTargetSaveReqVO ruleTargetSaveReqVO);

    void changeStatus(@Valid RuleTargetChangeStatusReqVO ruleTargetChangeStatusReqVO);
}
