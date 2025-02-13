package com.liboshuai.slr.server.biz.controller.rule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleTargetPageReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleTargetSaveReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleTargetRespVO;
import com.liboshuai.slr.server.biz.service.rule.RuleTargetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "风控规则目标")
@RequestMapping("/ruleTarget")
public class RuleTargetController {
    private final RuleTargetService ruleTargetService;

    @PostMapping("/page")
    @Operation(summary = "分页")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则目标分页请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleTargetPageReqVO.class)))
    public CommonResult<PageResult<RuleTargetRespVO>> page(@RequestBody @Valid RuleTargetPageReqVO ruleTargetPageReqVO) {
        PageResult<RuleTargetRespVO> ruleKeyRespVOPageResult = ruleTargetService.page(ruleTargetPageReqVO);
        return success(ruleKeyRespVOPageResult);
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<RuleTargetRespVO> detail(@NotBlank String targetCode) {
        RuleTargetRespVO ruletargetRespVO = ruleTargetService.detail(targetCode);
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

    @GetMapping("/list")
    @Operation(summary = "列表")
    public CommonResult<List<RuleTargetRespVO>> list(@NotBlank String channel) {
        List<RuleTargetRespVO> ruleTargetRespVOList = ruleTargetService.list(channel);
        return success(ruleTargetRespVOList);
    }

    @GetMapping("/listDetail")
    @Operation(summary = "详情列表")
    public CommonResult<List<RuleTargetRespVO>> listDetail() {
        List<RuleTargetRespVO> ruleTargetRespVOList = ruleTargetService.listDetail();
        return success(ruleTargetRespVOList);
    }

    @GetMapping("/checkUniqueTargetCode")
    @Operation(summary = "检查目标编号是否唯一")
    public CommonResult<Boolean> checkUniqueTargetCode(String targetCode) {
        Boolean result = ruleTargetService.checkUniqueTargetCode(targetCode);
        return success(result);
    }
}
