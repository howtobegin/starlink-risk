package com.liboshuai.slr.server.biz.rest.server.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.server.biz.controller.rule.RuleEventAttrController;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleEventAttrRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "风控规则事件属性server")
@RequestMapping("/ruleEventAttrServer")
public class RuleEventAttrServer {
    private final RuleEventAttrController ruleEventAttrController;

    @GetMapping("/list")
    @Operation(summary = "列表")
    public CommonResult<List<RuleEventAttrRespVO>> list(@NotBlank String eventCode) {
        return ruleEventAttrController.list(eventCode);
    }

    @GetMapping("/checkUniqueEventAttrCode")
    @Operation(summary = "检查事件属性编号是否唯一")
    public CommonResult<Boolean> checkUniqueEventAttrCode(@NotBlank String eventAttrCode) {
        return ruleEventAttrController.checkUniqueEventAttrCode(eventAttrCode);
    }
}
