package com.liboshuai.slr.server.biz.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleInfoChangeStatusReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleInfoPageReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleInfoSaveReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.resp.RuleInfoRespVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public interface RuleInfoService {

    PageResult<RuleInfoRespVO> page(@Valid RuleInfoPageReqVO ruleInfoPageReqVO);

    RuleInfoRespVO detail(@NotBlank Long ruleCode);

    Long create(@Valid RuleInfoSaveReqVO ruleInfoSaveReqVO);

    void update(@Valid RuleInfoSaveReqVO ruleInfoSaveReqVO);

    void changeStatus(@Valid RuleInfoChangeStatusReqVO ruleInfoChangeStatusReqVO);

    RuleInfoDTO buildRuleInfoDTO(Long ruleCode);

    void putCacheRuleInfo(Long ruleCode);

    RuleInfoDTO getCacheRuleInfo(Long ruleCode);

    void refreshCache();

    Boolean validateFlink(Long ruleCode);
}
