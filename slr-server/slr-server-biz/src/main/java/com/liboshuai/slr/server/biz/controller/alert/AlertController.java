package com.liboshuai.slr.server.biz.controller.alert;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.server.biz.controller.alert.vo.AlertPageReqVO;
import com.liboshuai.slr.server.biz.controller.alert.vo.AlertRespVO;
import com.liboshuai.slr.server.biz.service.alert.AlertService;
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
@RequestMapping("/alert")
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/page")
    @Operation(summary = "分页")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "预警消息列表请求", required = true,
            content = @Content(schema = @Schema(implementation = AlertPageReqVO.class)))
    public CommonResult<PageResult<AlertRespVO>> page(@RequestBody @Valid AlertPageReqVO alertPageReqVO) {
        PageResult<AlertRespVO> messageRespVOPageResult = alertService.page(alertPageReqVO);
        PageResult<AlertRespVO> alertRespVOPageResult = BeanUtils.toBean(messageRespVOPageResult, AlertRespVO.class);
        return success(alertRespVOPageResult);
    }
}
