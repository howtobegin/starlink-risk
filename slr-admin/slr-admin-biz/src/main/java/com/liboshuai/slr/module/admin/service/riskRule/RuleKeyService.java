package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeyPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeySaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleKeyRespVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface RuleKeyService {

    PageResult<RuleKeyRespVO> list(@Valid RuleKeyPageReqVO ruleKeyPageReqVO);

    RuleKeyRespVO detail(@NotBlank String keyCode);

    void create(@Valid RuleKeySaveReqVO ruleKeySaveReqVO);

    void update(@Valid RuleKeySaveReqVO ruleKeySaveReqVO);

    boolean checkUniqueKeyCode(@NotNull Long keyId, @NotBlank String keyCode);

    boolean checkUniqueEventCode(@NotNull Long eventId, @NotBlank String eventCode);
}
