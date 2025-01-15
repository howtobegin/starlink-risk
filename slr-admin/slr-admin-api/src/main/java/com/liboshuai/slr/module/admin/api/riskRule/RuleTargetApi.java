package com.liboshuai.slr.module.admin.api.riskRule;

import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleTargetApiDTO;

import java.util.List;

public interface RuleTargetApi {

    List<RuleTargetApiDTO> getCacheDetailList();
}
