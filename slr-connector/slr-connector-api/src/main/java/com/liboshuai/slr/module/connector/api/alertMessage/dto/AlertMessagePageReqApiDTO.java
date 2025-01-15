package com.liboshuai.slr.module.connector.api.alertMessage.dto;

import com.liboshuai.slr.framework.common.pojo.PageDTO;
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
public class AlertMessagePageReqApiDTO extends PageDTO {

    private static final long serialVersionUID = 1;

    /**
     * 渠道
     */
    @NotBlank(message = "渠道[channel]，不能为空")
    private String channel;

    /**
     * 规则编号
     */
    private String ruleCode;

    /**
     * 预警消息
     */
    private String alertMessage;

    /**
     * 预警时间开始
     */
    private LocalDateTime alertTimeStart;

    /**
     * 预警时间结束
     */
    private LocalDateTime alertTimeEnd;
}
