package com.liboshuai.slr.module.admin.api.riskRule;

import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleInfoApiDTO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RuleInfoApiImpl implements RuleInfoApi {
    @Resource
    private RuleInfoService ruleInfoService;

    @Override
    public RuleInfoApiDTO getCacheRuleInfo(Long ruleCode) {
        RuleInfoDTO cacheRuleInfo = ruleInfoService.getCacheRuleInfo(ruleCode);
        return BeanUtils.toBean(cacheRuleInfo, RuleInfoApiDTO.class);
    }
}
