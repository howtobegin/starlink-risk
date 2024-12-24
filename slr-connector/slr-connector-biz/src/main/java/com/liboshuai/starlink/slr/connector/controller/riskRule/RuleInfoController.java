package com.liboshuai.starlink.slr.connector.controller.riskRule;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "kafka事件接口")
@RequestMapping("/kafkaEvent")
public class RuleInfoController {

//    @Resource
//    private RuleInfoService ruleInfoService;
//
//    @RateLimiter
//    @PostMapping("/list")
//    @Operation(summary = "列表")
//    public CommonResult<?> list() {
//        ruleInfoService.list();
//        return success();
//    }
}
