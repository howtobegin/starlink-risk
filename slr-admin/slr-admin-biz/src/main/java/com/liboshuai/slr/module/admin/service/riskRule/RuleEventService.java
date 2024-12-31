package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleEventChangeStatusReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventRespVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

public interface RuleEventService {

    void changeStatus(@Valid RuleEventChangeStatusReqVO ruleEventChangeStatusReqVO);

    List<RuleEventRespVO> list(@NotBlank String targetCode);
}
