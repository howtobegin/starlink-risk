package com.liboshuai.slr.module.admin.controller.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.req.RuleEventChangeStatusReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventRespVO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@Tag(name = "风控规则事件")
@RequestMapping("/ruleEvent")
public class RuleEventController {
    @Resource
    private RuleEventService ruleEventService;

    @PostMapping(value = "/changeStatus")
    @Operation(summary = "变更状态")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "规则目标变更状态请求", required = true,
            content = @Content(schema = @Schema(implementation = RuleEventChangeStatusReqVO.class)))
    public CommonResult<Boolean> changeStatus(@RequestBody @Valid RuleEventChangeStatusReqVO ruleEventChangeStatusReqVO) {
        ruleEventService.changeStatus(ruleEventChangeStatusReqVO);
        return success(true);
    }

    @PostMapping("/list")
    @Operation(summary = "列表")
    public CommonResult<List<RuleEventRespVO>> list(@NotBlank String targetCode) {
        List<RuleEventRespVO> ruleEventRespVOList = ruleEventService.list(targetCode);
        return success(ruleEventRespVOList);
    }
}
