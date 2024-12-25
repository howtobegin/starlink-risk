package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.pojo.vo.riskRule.RuleInfoReqVO;
import com.liboshuai.slr.module.admin.pojo.vo.riskRule.RuleInfoRespVO;

public interface RuleInfoService {

    PageResult<RuleInfoRespVO> list(RuleInfoReqVO ruleInfoReqVO);
}
