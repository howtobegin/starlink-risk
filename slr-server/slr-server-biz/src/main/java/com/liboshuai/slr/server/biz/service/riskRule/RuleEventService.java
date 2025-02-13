package com.liboshuai.slr.server.biz.service.riskRule;

import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleEventChangeStatusReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleEventRespVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

public interface RuleEventService {

    void changeStatus(@Valid RuleEventChangeStatusReqVO ruleEventChangeStatusReqVO);

    List<RuleEventRespVO> list(@NotBlank String targetCode);

    Boolean checkUniqueEventCode(String eventCode);
}
