package com.liboshuai.slr.module.admin.controller.alertMessage.vo;

import com.liboshuai.slr.framework.common.pojo.PageParam;
import com.liboshuai.slr.framework.common.validation.InStringEnum;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
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
public class AlertMessagePageReqVO extends PageParam {

    private static final long serialVersionUID = 1;

    @NotBlank(message = "渠道[channel]，不能为空")
    @InStringEnum(value = ChannelEnum.class, message = "渠道[channel]，必须在指定范围 {value}")
    @Schema(description = "渠道", example = "GAME")
    private String channel;

    @Schema(description = "规则编号", example = "R175928847299117063")
    private String ruleCode;

    @Schema(description = "预警消息", example = "触发了xxx预警")
    private String alertMessage;

    @Schema(description = "预警时间开始", example = "2025-01-01 00:01:01")
    private LocalDateTime alertTimeStart;

    @Schema(description = "预警时间结束", example = "2025-01-02 00:01:01")
    private LocalDateTime alertTimeEnd;
}
