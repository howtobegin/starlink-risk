package com.liboshuai.slr.server.biz.rest.server.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.server.biz.controller.riskRule.RuleTargetController;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleTargetPageReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleTargetSaveReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.resp.RuleTargetRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "风控规则目标server")
@RequestMapping("/ruleTargetServer")
public class RuleTargetServer {
    private final RuleTargetController ruleTargetController;

    @PostMapping("/page")
    @Operation(summary = "分页")
    public CommonResult<PageResult<RuleTargetRespVO>> page(@RequestBody @NotBlank String json) {
        return ruleTargetController.page(JsonUtils.parseObject(json, RuleTargetPageReqVO.class));
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<RuleTargetRespVO> detail(@NotBlank String targetCode) {
        return ruleTargetController.detail(targetCode);
    }

    @PostMapping(value = "/create")
    @Operation(summary = "新增")
    public CommonResult<Boolean> create(@RequestBody @NotBlank String json) {
        ruleTargetController.create(JsonUtils.parseObject(json, RuleTargetSaveReqVO.class));
        return success(true);
    }

    @PostMapping(value = "/update")
    @Operation(summary = "更新")
    public CommonResult<Boolean> update(@RequestBody @NotBlank String json) {
        ruleTargetController.update(JsonUtils.parseObject(json, RuleTargetSaveReqVO.class));
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "列表")
    public CommonResult<List<RuleTargetRespVO>> list(@NotBlank String channel) {
        return ruleTargetController.list(channel);
    }

    @GetMapping("/listDetail")
    @Operation(summary = "详情列表")
    public CommonResult<List<RuleTargetRespVO>> listDetail() {
        return ruleTargetController.listDetail();
    }

    @GetMapping("/checkUniqueTargetCode")
    @Operation(summary = "检查目标编号是否唯一")
    public CommonResult<Boolean> checkUniqueTargetCode(@NotBlank String targetCode) {
        return ruleTargetController.checkUniqueTargetCode(targetCode);
    }
}
