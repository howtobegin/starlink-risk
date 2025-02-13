package com.liboshuai.slr.server.biz.controller.alert.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liboshuai.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.slr.framework.common.pojo.PageParam;
import com.liboshuai.slr.framework.common.validation.InStringEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 预警信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AlertPageReqVO extends PageParam {

    private static final long serialVersionUID = 1;

    @InStringEnum(value = ChannelEnum.class, message = "渠道[channel]，必须在指定范围 {value}")
    @Schema(description = "渠道", example = "GAME", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String channel;

    @Schema(description = "规则编号", example = "R175928847299117063", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String ruleCode;

    @Schema(description = "预警消息", example = "触发了xxx预警", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String message;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "预警时间开始", example = "2025-01-01 00:01:01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime timeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "预警时间结束", example = "2025-01-02 00:01:01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime timeEnd;
}
