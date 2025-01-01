package com.liboshuai.slr.module.admin.api.riskRule;

import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleTargetDTO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleTargetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RuleTargetApiImpl implements RuleTargetApi {
    @Resource
    private RuleTargetService ruleTargetService;

    @Override
    public List<RuleTargetDTO> getCacheDetailList() {
        return ruleTargetService.getCacheDetailList();
    }
}
