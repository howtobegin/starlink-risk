package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoChangeStatusReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleInfoRespVO;

import javax.validation.Valid;

public interface RuleInfoService {

    PageResult<RuleInfoRespVO> list(RuleInfoPageReqVO ruleInfoPageReqVO);

    RuleInfoRespVO detail(String ruleCode);

    String create(RuleInfoSaveReqVO ruleInfoSaveReqVO);

    void update(@Valid RuleInfoSaveReqVO ruleInfoSaveReqVO);

    void changeStatus(@Valid RuleInfoChangeStatusReqVO ruleInfoChangeStatusReqVO);
}
