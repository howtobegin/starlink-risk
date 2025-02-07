package com.liboshuai.slr.server.biz.rest.server.riskRule;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.server.biz.controller.alertMessage.AlertMessageController;
import com.liboshuai.slr.server.biz.controller.alertMessage.vo.AlertMessagePageReqVO;
import com.liboshuai.slr.server.biz.controller.alertMessage.vo.AlertMessageRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "风控预警消息server")
@RequestMapping("/alertMessageServer")
public class AlertMessageServer {

    private final AlertMessageController alertMessageController;

    @PostMapping("/page")
    @Operation(summary = "分页")
    public CommonResult<PageResult<AlertMessageRespVO>> page(@RequestBody @NotBlank String json) {
        return alertMessageController.page(JsonUtils.parseObject(json, AlertMessagePageReqVO.class));
    }
}
