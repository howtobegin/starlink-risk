package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.CheckUniqueEventCodeReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.CheckUniqueKeyCodeReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeyPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeySaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleKeyRespVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public interface RuleKeyService {

    PageResult<RuleKeyRespVO> list(@Valid RuleKeyPageReqVO ruleKeyPageReqVO);

    RuleKeyRespVO detail(@NotBlank String keyCode);

    void create(@Valid RuleKeySaveReqVO ruleKeySaveReqVO);

    void update(@Valid RuleKeySaveReqVO ruleKeySaveReqVO);

    boolean checkUniqueKeyCode(@Valid CheckUniqueKeyCodeReqVO checkUniqueKeyCodeReqVO);

    boolean checkUniqueEventCode(@Valid CheckUniqueEventCodeReqVO checkUniqueEventCodeReqVO);
}
