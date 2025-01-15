package com.liboshuai.slr.module.admin.api.riskRule;

import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleInfoApiDTO;

public interface RuleInfoApi {

    RuleInfoApiDTO getCacheRuleInfo(Long ruleCode);
}
