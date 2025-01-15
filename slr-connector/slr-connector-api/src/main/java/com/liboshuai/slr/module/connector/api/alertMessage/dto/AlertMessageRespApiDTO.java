package com.liboshuai.slr.module.connector.api.alertMessage.dto;

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
public class AlertMessageRespApiDTO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 渠道
     */
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
     * 预警时间
     */
    private LocalDateTime alertTime;
}
