package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleModelCreateReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleModelPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleModelUpdateReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleModelRespVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public interface RuleModelService {
    PageResult<RuleModelRespVO> page(@Valid RuleModelPageReqVO ruleModelPageReqVO);

    RuleModelRespVO detail(@NotBlank Long modelCode);

    Long create(@Valid RuleModelCreateReqVO ruleModelCreateReqVO);

    void update(@Valid RuleModelUpdateReqVO ruleModelUpdateReqVO);
}
