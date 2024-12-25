package com.liboshuai.starlink.slr.admin.service.riskRule;

import com.liboshuai.starlink.slr.admin.pojo.vo.riskRule.RuleInfoReqVO;
import com.liboshuai.starlink.slr.admin.pojo.vo.riskRule.RuleInfoRespVO;
import com.liboshuai.starlink.slr.framework.common.pojo.PageResult;

public interface RuleInfoService {

    PageResult<RuleInfoRespVO> list(RuleInfoReqVO ruleInfoReqVO);
}
