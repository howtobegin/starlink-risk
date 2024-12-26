package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleInfoReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleInfoRespVO;

public interface RuleInfoService {

    PageResult<RuleInfoRespVO> list(RuleInfoReqVO ruleInfoReqVO);

    RuleInfoRespVO detail(String ruleCode);
}
