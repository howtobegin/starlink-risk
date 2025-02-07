package com.liboshuai.slr.server.biz.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleTargetPageReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleTargetSaveReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.resp.RuleTargetRespVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

public interface RuleTargetService {

    PageResult<RuleTargetRespVO> page(@Valid RuleTargetPageReqVO ruleTargetPageReqVO);

    RuleTargetRespVO detail(@NotBlank String targetCode);

    void create(@Valid RuleTargetSaveReqVO ruleTargetSaveReqVO);

    void update(@Valid RuleTargetSaveReqVO ruleTargetSaveReqVO);

    List<RuleTargetRespVO> list(@NotBlank String channel);

    Boolean checkUniqueTargetCode(String targetCode);

    List<RuleTargetRespVO> listDetail();
}
