package com.liboshuai.slr.module.admin.controller.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.ratelimiter.core.annotation.RateLimiter;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleInfoReqVO;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.RuleInfoRespVO;
import com.liboshuai.slr.module.admin.service.riskRule.RuleInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@Tag(name = "规则信息管理")
@RequestMapping("/ruleInfo")
public class RuleInfoController {

    @Resource
    private RuleInfoService ruleInfoService;

    @RateLimiter
    @PostMapping("/list")
    @Operation(summary = "获取规则信息列表")
    public CommonResult<PageResult<RuleInfoRespVO>> list(@RequestBody RuleInfoReqVO ruleInfoReqVO) {
        PageResult<RuleInfoRespVO> ruleInfoPage = ruleInfoService.list(ruleInfoReqVO);
        return success(ruleInfoPage);
    }
}
