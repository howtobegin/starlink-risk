package com.liboshuai.slr.module.admin.service.riskRule;

import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventAttrRespVO;

import javax.validation.constraints.NotBlank;
import java.util.List;

public interface RuleEventAttrService {
    List<RuleEventAttrRespVO> list(@NotBlank String eventCode);
}
