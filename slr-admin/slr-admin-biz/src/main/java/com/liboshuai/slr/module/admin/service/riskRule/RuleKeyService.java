package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeyPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeySaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleKeyRespVO;

public interface RuleKeyService {

    PageResult<RuleKeyRespVO> list(RuleKeyPageReqVO ruleKeyPageReqVO);

    RuleKeyRespVO detail(String keyCode);

    void create(RuleKeySaveReqVO ruleKeySaveReqVO);

    void update(RuleKeySaveReqVO ruleKeySaveReqVO);
}
