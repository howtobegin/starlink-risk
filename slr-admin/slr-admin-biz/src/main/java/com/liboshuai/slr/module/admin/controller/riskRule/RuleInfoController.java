package com.liboshuai.slr.module.admin.controller.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoChangeStatusReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleInfoRespVO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
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
@Tag(name = "风控规则信息")
@RequestMapping("/ruleInfo")
public class RuleInfoController {

    @Resource
    private RuleInfoService ruleInfoService;

    @PostMapping("/list")
    @Operation(summary = "列表")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则信息列表请求", required = true, content = @Content(schema = @Schema(implementation = RuleInfoPageReqVO.class)))
    public CommonResult<PageResult<RuleInfoRespVO>> list(@RequestBody @Valid RuleInfoPageReqVO ruleInfoPageReqVO) {
        PageResult<RuleInfoRespVO> ruleInfoPage = ruleInfoService.list(ruleInfoPageReqVO);
        return success(ruleInfoPage);
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<RuleInfoRespVO> detail(@NotBlank String ruleCode) {
        RuleInfoRespVO ruleInfoRespVO = ruleInfoService.detail(ruleCode);
        return success(ruleInfoRespVO);
    }

    @PostMapping(value = "/create")
    @Operation(summary = "新增")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则信息保存请求", required = true, content = @Content(schema = @Schema(implementation = RuleInfoSaveReqVO.class)))
    public CommonResult<String> create(@RequestBody @Valid RuleInfoSaveReqVO ruleInfoSaveReqVO) {
        String ruleCode = ruleInfoService.create(ruleInfoSaveReqVO);
        return success(ruleCode);
    }

    @PostMapping(value = "/update")
    @Operation(summary = "更新")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则信息更新请求", required = true, content = @Content(schema = @Schema(implementation = RuleInfoSaveReqVO.class)))
    public CommonResult<Boolean> update(@RequestBody @Valid RuleInfoSaveReqVO ruleInfoSaveReqVO) {
        ruleInfoService.update(ruleInfoSaveReqVO);
        return success(true);
    }

    @PostMapping(value = "/changeStatus")
    @Operation(summary = "变更状态")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则信息变更状态请求", required = true, content = @Content(schema = @Schema(implementation = RuleInfoChangeStatusReqVO.class)))
    public CommonResult<Boolean> changeStatus(@RequestBody @Valid RuleInfoChangeStatusReqVO ruleInfoChangeStatusReqVO) {
        ruleInfoService.changeStatus(ruleInfoChangeStatusReqVO);
        return success(true);
    }
}
