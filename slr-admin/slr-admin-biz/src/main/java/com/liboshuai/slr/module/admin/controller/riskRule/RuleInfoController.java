package com.liboshuai.slr.module.admin.controller.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleInfoSaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleInfoRespVO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "规则信息管理")
@RequestMapping("/ruleInfo")
public class RuleInfoController {

    @Resource
    private RuleInfoService ruleInfoService;

    @PostMapping("/list")
    @Operation(summary = "获取规则信息列表")
    public CommonResult<PageResult<RuleInfoRespVO>> list(@RequestBody @Valid RuleInfoPageReqVO ruleInfoPageReqVO) {
        PageResult<RuleInfoRespVO> ruleInfoPage = ruleInfoService.list(ruleInfoPageReqVO);
        return success(ruleInfoPage);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取规则信息详情")
    public CommonResult<RuleInfoRespVO> detail(@NotBlank String ruleCode) {
        RuleInfoRespVO ruleInfoRespVO = ruleInfoService.detail(ruleCode);
        return success(ruleInfoRespVO);
    }

    @GetMapping("/create")
    @Operation(summary = "新增规则信息")
    public CommonResult<String> create(@RequestBody @Valid RuleInfoSaveReqVO ruleInfoSaveReqVO) {
        String ruleCode = ruleInfoService.create(ruleInfoSaveReqVO);
        return success(ruleCode);
    }
}
