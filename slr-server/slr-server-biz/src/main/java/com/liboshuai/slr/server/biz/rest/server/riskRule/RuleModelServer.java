package com.liboshuai.slr.server.biz.rest.server.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.server.biz.controller.riskRule.RuleModelController;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleModelCreateReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleModelPageReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleModelUpdateReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.resp.RuleModelRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "风控规则模型server")
@RequestMapping("/ruleModelServer")
public class RuleModelServer {

    private final RuleModelController ruleModelController;

    @PostMapping("/page")
    @Operation(summary = "分页")
    public CommonResult<PageResult<RuleModelRespVO>> page(@RequestBody @NotBlank String json) {
        return ruleModelController.page(JsonUtils.parseObject(json, RuleModelPageReqVO.class));
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<RuleModelRespVO> detail(@NotNull Long modelCode) {
        return ruleModelController.detail(modelCode);
    }

    @PostMapping(value = "/create")
    @Operation(summary = "新增")
    public CommonResult<Long> create(@RequestBody @NotBlank String json) {
        return ruleModelController.create(JsonUtils.parseObject(json, RuleModelCreateReqVO.class));
    }

    @PostMapping(value = "/update")
    @Operation(summary = "更新")
    public CommonResult<Boolean> update(@RequestBody @NotBlank String json) {
        ruleModelController.update(JsonUtils.parseObject(json, RuleModelUpdateReqVO.class));
        return success(true);
    }
}
