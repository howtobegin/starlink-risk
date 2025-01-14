package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoChangeStatusReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleInfoRespVO;
import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public interface RuleInfoService {

    PageResult<RuleInfoRespVO> page(@Valid RuleInfoPageReqVO ruleInfoPageReqVO);

    RuleInfoRespVO detail(@NotBlank Long ruleCode);

    Long create(@Valid RuleInfoSaveReqVO ruleInfoSaveReqVO);

    void update(@Valid RuleInfoSaveReqVO ruleInfoSaveReqVO);

    void changeStatus(@Valid RuleInfoChangeStatusReqVO ruleInfoChangeStatusReqVO);

    RuleInfoDTO buildRuleInfoDTO(Long ruleCode);

    Boolean validateFlink(Long ruleCode);
}
