package com.liboshuai.slr.server.biz.rest.server.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.server.biz.controller.rule.RuleEventController;
import com.liboshuai.slr.server.biz.controller.rule.vo.req.RuleEventChangeStatusReqVO;
import com.liboshuai.slr.server.biz.controller.rule.vo.resp.RuleEventRespVO;
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
@Tag(name = "风控规则事件server")
@RequestMapping("/ruleEventServer")
public class RuleEventServer {
    private final RuleEventController ruleEventController;

    @PostMapping(value = "/changeStatus")
    @Operation(summary = "变更状态")
    public CommonResult<Boolean> changeStatus(@RequestBody @NotBlank String json) {
        ruleEventController.changeStatus(JsonUtils.parseObject(json, RuleEventChangeStatusReqVO.class));
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "列表")
    public CommonResult<List<RuleEventRespVO>> list(@NotBlank String targetCode) {
        return ruleEventController.list(targetCode);
    }

    @GetMapping("/checkUniqueEventCode")
    @Operation(summary = "检查事件编号是否唯一")
    public CommonResult<Boolean> checkUniqueEventCode(@NotBlank String eventCode) {
        return ruleEventController.checkUniqueEventCode(eventCode);
    }
}
