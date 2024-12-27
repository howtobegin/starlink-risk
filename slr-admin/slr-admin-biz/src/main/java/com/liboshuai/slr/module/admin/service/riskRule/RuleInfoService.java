package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleInfoRespVO;

public interface RuleInfoService {

    PageResult<RuleInfoRespVO> list(RuleInfoPageReqVO ruleInfoPageReqVO);

    RuleInfoRespVO detail(String ruleCode);

    String create(RuleInfoSaveReqVO ruleInfoSaveReqVO);
}
