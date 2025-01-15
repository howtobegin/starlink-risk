package com.liboshuai.slr.module.admin.controller.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventAttrRespVO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleEventAttrService;
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

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "风控规则事件属性")
@RequestMapping("/ruleEventAttr")
public class RuleEventAttrController {
    private final RuleEventAttrService ruleEventAttrService;

    @GetMapping("/list")
    @Operation(summary = "列表")
    public CommonResult<List<RuleEventAttrRespVO>> list(@NotBlank String eventCode) {
        List<RuleEventAttrRespVO> ruleEventAttrRespVOList = ruleEventAttrService.list(eventCode);
        return success(ruleEventAttrRespVOList);
    }

    @GetMapping("/checkUniqueEventAttrCode")
    @Operation(summary = "检查事件属性编号是否唯一")
    public CommonResult<Boolean> checkUniqueEventAttrCode(String eventAttrCode) {
        Boolean result = ruleEventAttrService.checkUniqueEventAttrCode(eventAttrCode);
        return success(result);
    }
}
