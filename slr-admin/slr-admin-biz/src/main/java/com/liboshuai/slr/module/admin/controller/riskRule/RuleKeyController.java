package com.liboshuai.slr.module.admin.controller.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.CheckUniqueEventCodeReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.CheckUniqueKeyCodeReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeyPageReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleKeySaveReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleKeyRespVO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleKeyService;
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
@Tag(name = "规则目标管理")
@RequestMapping("/ruleModel")
public class RuleKeyController {
    @Resource
    private RuleKeyService ruleKeyService;

    @PostMapping("/list")
    @Operation(summary = "列表")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则目标列表请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleKeyPageReqVO.class)))
    public CommonResult<PageResult<RuleKeyRespVO>> list(@RequestBody @Valid RuleKeyPageReqVO ruleKeyPageReqVO) {
        PageResult<RuleKeyRespVO> ruleKeyRespVOPageResult = ruleKeyService.list(ruleKeyPageReqVO);
        return success(ruleKeyRespVOPageResult);
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<RuleKeyRespVO> detail(@NotBlank String keyCode) {
        RuleKeyRespVO ruleKeyRespVO = ruleKeyService.detail(keyCode);
        return success(ruleKeyRespVO);
    }

    @PostMapping(value = "/create")
    @Operation(summary = "新增")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则目标保存请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleKeySaveReqVO.class)))
    public CommonResult<Boolean> create(@RequestBody @Valid RuleKeySaveReqVO ruleKeySaveReqVO) {
        ruleKeyService.create(ruleKeySaveReqVO);
        return success(true);
    }

    @PostMapping(value = "/update")
    @Operation(summary = "更新")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则目标保存请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleKeySaveReqVO.class)))
    public CommonResult<Boolean> update(@RequestBody @Valid RuleKeySaveReqVO ruleKeySaveReqVO) {
        ruleKeyService.update(ruleKeySaveReqVO);
        return success(true);
    }

    @PostMapping(value = "/checkUniqueKeyCode")
    @Operation(summary = "检查目标编号是否唯一")
    public CommonResult<Boolean> checkUniqueKeyCode(@RequestBody @Valid CheckUniqueKeyCodeReqVO checkUniqueKeyCodeReqVO) {
        boolean checkResult = ruleKeyService.checkUniqueKeyCode(checkUniqueKeyCodeReqVO);
        return success(checkResult);
    }

    @PostMapping(value = "/checkUniqueEventCode")
    @Operation(summary = "检查事件编号是否唯一")
    public CommonResult<Boolean> checkUniqueEventCode(@RequestBody @Valid CheckUniqueEventCodeReqVO checkUniqueEventCodeReqVO) {
        boolean checkResult = ruleKeyService.checkUniqueEventCode(checkUniqueEventCodeReqVO);
        return success(checkResult);
    }
}
