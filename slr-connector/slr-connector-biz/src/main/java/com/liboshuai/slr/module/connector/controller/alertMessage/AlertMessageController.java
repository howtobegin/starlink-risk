package com.liboshuai.slr.module.connector.controller.alertMessage;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.module.connector.controller.alertMessage.vo.AlertMessageReqVO;
import com.liboshuai.slr.module.connector.controller.alertMessage.vo.AlertMessageRespVO;
import com.liboshuai.slr.module.connector.service.alertMessage.AlertMessageService;
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

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@Tag(name = "风控预警消息")
@RequestMapping("/alertMessage")
public class AlertMessageController {

    @Resource
    private AlertMessageService alertMessageService;

    @PostMapping("/list")
    @Operation(summary = "列表")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "预警消息列表请求", required = true,
            content = @Content(schema = @Schema(implementation = AlertMessageReqVO.class)))
    public CommonResult<PageResult<AlertMessageRespVO>> list(@RequestBody @Validated AlertMessageReqVO alertMessageReqVO) {
        return success(alertMessageService.list(alertMessageReqVO));
    }
}
