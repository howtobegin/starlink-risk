package com.liboshuai.slr.module.admin.controller.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleTargetPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleTargetSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleTargetRespVO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleTargetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@Tag(name = "风控规则目标")
@RequestMapping("/ruleTarget")
public class RuleTargetController {
    @Resource
    private RuleTargetService ruleTargetService;

    @PostMapping("/list")
    @Operation(summary = "列表")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则目标列表请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleTargetPageReqVO.class)))
    public CommonResult<PageResult<RuleTargetRespVO>> list(@RequestBody @Valid RuleTargetPageReqVO ruleTargetPageReqVO) {
        PageResult<RuleTargetRespVO> ruleKeyRespVOPageResult = ruleTargetService.list(ruleTargetPageReqVO);
        return success(ruleKeyRespVOPageResult);
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<RuleTargetRespVO> detail(@NotBlank String keyCode) {
        RuleTargetRespVO ruletargetRespVO = ruleTargetService.detail(keyCode);
        return success(ruletargetRespVO);
    }

    @PostMapping(value = "/create")
    @Operation(summary = "新增")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则目标保存请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleTargetSaveReqVO.class)))
    public CommonResult<Boolean> create(@RequestBody @Valid RuleTargetSaveReqVO ruleTargetSaveReqVO) {
        ruleTargetService.create(ruleTargetSaveReqVO);
        return success(true);
    }

    @PostMapping(value = "/update")
    @Operation(summary = "更新")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则目标保存请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleTargetSaveReqVO.class)))
    public CommonResult<Boolean> update(@RequestBody @Valid RuleTargetSaveReqVO ruleTargetSaveReqVO) {
        ruleTargetService.update(ruleTargetSaveReqVO);
        return success(true);
    }
}
