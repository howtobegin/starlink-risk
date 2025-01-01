package com.liboshuai.slr.module.admin.api.riskRule;

import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleTargetDTO;

import java.util.List;

public interface RuleTargetApi {

    List<RuleTargetDTO> getCacheDetailList();
}
