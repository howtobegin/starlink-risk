package com.liboshuai.slr.server.biz.service.rule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleModelCreateReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleModelPageReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleModelUpdateReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleModelRespVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public interface RuleModelService {
    PageResult<RuleModelRespVO> page(@Valid RuleModelPageReqVO ruleModelPageReqVO);

    RuleModelRespVO detail(@NotBlank Long modelCode);

    Long create(@Valid RuleModelCreateReqVO ruleModelCreateReqVO);

    void update(@Valid RuleModelUpdateReqVO ruleModelUpdateReqVO);
}
