package com.liboshuai.slr.module.connector.controller.alertMessage.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预警信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AlertMessageRespVO implements Serializable {

    private static final long serialVersionUID = 1;

    @Schema(description = "渠道", example = "GAME")
    private String channel;

    @Schema(description = "规则编号", example = "R175928847299117063")
    private String ruleCode;

    @Schema(description = "预警消息", example = "触发了xxx预警")
    private String alertMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "预警时间", example = "2025-01-01 00:01:01")
    private LocalDateTime alertTime;
}
