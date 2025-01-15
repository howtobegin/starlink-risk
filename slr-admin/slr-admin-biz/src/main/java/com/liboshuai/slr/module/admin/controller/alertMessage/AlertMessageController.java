package com.liboshuai.slr.module.admin.controller.alertMessage;

import com.liboshuai.slr.framework.common.pojo.CommonResult;
import com.liboshuai.slr.framework.common.pojo.PageResult;
import com.liboshuai.slr.framework.common.util.object.BeanUtils;
import com.liboshuai.slr.module.admin.controller.alertMessage.vo.AlertMessagePageReqVO;
import com.liboshuai.slr.module.admin.controller.alertMessage.vo.AlertMessageRespVO;
import com.liboshuai.slr.module.connector.api.alertMessage.AlertMessageApi;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessagePageReqApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageRespApiDTO;
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

import static com.liboshuai.slr.framework.common.pojo.CommonResult.success;

@Slf4j
@Validated
@RestController
@Tag(name = "风控预警消息")
@RequestMapping("/alertMessage")
public class AlertMessageController {

    @Resource
    private AlertMessageApi alertMessageApi;

    @PostMapping("/page")
    @Operation(summary = "分页")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "预警消息列表请求", required = true,
            content = @Content(schema = @Schema(implementation = AlertMessagePageReqVO.class)))
    public CommonResult<PageResult<AlertMessageRespVO>> page(@RequestBody @Valid AlertMessagePageReqVO alertMessagePageReqVO) {
        AlertMessagePageReqApiDTO alertMessagePageReqApiDTO = BeanUtils.toBean(alertMessagePageReqVO, AlertMessagePageReqApiDTO.class);
        PageResult<AlertMessageRespApiDTO> alertMessageRespDTOPageResult = alertMessageApi.page(alertMessagePageReqApiDTO);
        PageResult<AlertMessageRespVO> alertMessageRespVOPageResult = BeanUtils.toBean(alertMessageRespDTOPageResult, AlertMessageRespVO.class);
        return success(alertMessageRespVOPageResult);
    }
}
