package com.liboshuai.slr.server.biz.service.rule;

import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleEventAttrRespVO;

import javax.validation.constraints.NotBlank;
import java.util.List;

public interface RuleEventAttrService {
    List<RuleEventAttrRespVO> list(@NotBlank String eventCode);

    Boolean checkUniqueEventAttrCode(String eventAttrCode);
}
