package com.liboshuai.slr.module.admin.controller.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleModelCreateReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleModelPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleModelUpdateReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleModelRespVO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "风控规则模型")
@RequestMapping("/ruleModel")
public class RuleModelController {

    private final RuleModelService ruleModelService;

    @PostMapping("/page")
    @Operation(summary = "分页")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则模型列表请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleModelPageReqVO.class)))
    public CommonResult<PageResult<RuleModelRespVO>> page(@RequestBody @Valid RuleModelPageReqVO ruleModelPageReqVO) {
        PageResult<RuleModelRespVO> ruleModelRespVOPageResult = ruleModelService.page(ruleModelPageReqVO);
        return success(ruleModelRespVOPageResult);
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<RuleModelRespVO> detail(@NotNull Long modelCode) {
        RuleModelRespVO ruleModelRespVO = ruleModelService.detail(modelCode);
        return success(ruleModelRespVO);
    }

    @PostMapping(value = "/create")
    @Operation(summary = "新增")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则模型保存请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleModelCreateReqVO.class)))
    public CommonResult<String> create(@RequestBody @Valid RuleModelCreateReqVO ruleModelCreateReqVO) {
        Long modelCode = ruleModelService.create(ruleModelCreateReqVO);
        return success(String.valueOf(modelCode));
    }

    @PostMapping(value = "/update")
    @Operation(summary = "更新")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则模型更新请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleModelUpdateReqVO.class)))
    public CommonResult<Boolean> update(@RequestBody @Valid RuleModelUpdateReqVO ruleModelUpdateReqVO) {
        ruleModelService.update(ruleModelUpdateReqVO);
        return success(true);
    }
}
