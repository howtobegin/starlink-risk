package com.liboshuai.slr.server.biz.controller.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.req.RuleEventChangeStatusReqVO;
import com.liboshuai.slr.server.biz.controller.riskRule.vo.resp.RuleEventRespVO;
import com.liboshuai.slr.server.biz.service.riskRule.RuleEventService;
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
@Tag(name = "风控规则事件")
@RequestMapping("/ruleEvent")
public class RuleEventController {
    private final RuleEventService ruleEventService;

    @PostMapping(value = "/changeStatus")
    @Operation(summary = "变更状态")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则目标变更状态请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleEventChangeStatusReqVO.class)))
    public CommonResult<Boolean> changeStatus(@RequestBody @Valid RuleEventChangeStatusReqVO ruleEventChangeStatusReqVO) {
        ruleEventService.changeStatus(ruleEventChangeStatusReqVO);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "列表")
    public CommonResult<List<RuleEventRespVO>> list(@NotBlank String targetCode) {
        List<RuleEventRespVO> ruleEventRespVOList = ruleEventService.list(targetCode);
        return success(ruleEventRespVOList);
    }

    @GetMapping("/checkUniqueEventCode")
    @Operation(summary = "检查事件编号是否唯一")
    public CommonResult<Boolean> checkUniqueEventCode(String eventCode) {
        Boolean result = ruleEventService.checkUniqueEventCode(eventCode);
        return success(result);
    }
}
