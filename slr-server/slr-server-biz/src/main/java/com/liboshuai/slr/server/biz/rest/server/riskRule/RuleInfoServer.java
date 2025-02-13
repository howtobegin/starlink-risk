package com.liboshuai.slr.server.biz.rest.server.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.server.biz.controller.rule.RuleInfoController;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleInfoChangeStatusReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleInfoPageReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleInfoSaveReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleInfoRespVO;
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
@Tag(name = "风控规则信息server")
@RequestMapping("/ruleInfoServer")
public class RuleInfoServer {

    private final RuleInfoController ruleInfoController;

    @PostMapping("/page")
    @Operation(summary = "分页")
    public CommonResult<PageResult<RuleInfoRespVO>> page(@RequestBody @NotBlank String json) {
        return ruleInfoController.page(JsonUtils.parseObject(json, RuleInfoPageReqVO.class));
    }

    @GetMapping("/detail")
    @Operation(summary = "详情")
    public CommonResult<RuleInfoRespVO> detail(@NotNull Long ruleCode) {
        return ruleInfoController.detail(ruleCode);
    }

    @PostMapping(value = "/create")
    @Operation(summary = "新增")
    public CommonResult<Long> create(@RequestBody @NotBlank String json) {
        return ruleInfoController.create(JsonUtils.parseObject(json, RuleInfoSaveReqVO.class));
    }

    @PostMapping(value = "/update")
    @Operation(summary = "更新")
    public CommonResult<Boolean> update(@RequestBody @NotBlank String json) {
        ruleInfoController.update(JsonUtils.parseObject(json, RuleInfoSaveReqVO.class));
        return success(true);
    }

    @PostMapping(value = "/changeStatus")
    @Operation(summary = "变更状态")
    public CommonResult<Boolean> changeStatus(@RequestBody @NotBlank String json) {
        ruleInfoController.changeStatus(JsonUtils.parseObject(json, RuleInfoChangeStatusReqVO.class));
        return success(true);
    }

    @GetMapping("/refreshCache")
    @Operation(summary = "刷新缓存")
    public CommonResult<Boolean> refreshCache() {
        ruleInfoController.refreshCache();
        return success(true);
    }

    @GetMapping(value = "/validateFlink")
    @Operation(summary = "验证flink")
    public CommonResult<Boolean> validateFlink(@NotNull Long ruleCode) {
        return ruleInfoController.validateFlink(ruleCode);
    }
}
