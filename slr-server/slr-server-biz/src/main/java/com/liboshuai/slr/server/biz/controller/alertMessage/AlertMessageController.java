package com.liboshuai.slr.server.biz.controller.alertMessage;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.server.biz.controller.alertMessage.vo.AlertMessagePageReqVO;
import com.liboshuai.slr.server.biz.controller.alertMessage.vo.AlertMessageRespVO;
import com.liboshuai.slr.server.biz.service.alertMessage.AlertMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "风控预警消息")
@RequestMapping("/alertMessage")
public class AlertMessageController {

    private final AlertMessageService alertMessageService;

    @PostMapping("/page")
    @Operation(summary = "分页")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "预警消息列表请求", required = true,
            content = @Content(schema = @Schema(implementation = AlertMessagePageReqVO.class)))
    public CommonResult<PageResult<AlertMessageRespVO>> page(@RequestBody @Valid AlertMessagePageReqVO alertMessagePageReqVO) {
        PageResult<AlertMessageRespVO> messageRespVOPageResult = alertMessageService.page(alertMessagePageReqVO);
        PageResult<AlertMessageRespVO> alertMessageRespVOPageResult = BeanUtils.toBean(messageRespVOPageResult, AlertMessageRespVO.class);
        return success(alertMessageRespVOPageResult);
    }
}
