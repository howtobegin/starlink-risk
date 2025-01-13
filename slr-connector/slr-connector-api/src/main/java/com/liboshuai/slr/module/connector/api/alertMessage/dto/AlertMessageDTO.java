package com.liboshuai.slr.module.connector.api.alertMessage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 预警信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AlertMessageDTO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 渠道
     */
    private String channel;
    /**
     * 规则编号
     */
    private Long ruleCode;
    /**
     * 预警消息
     */
    private String alertMessage;
    /**
     * 预警时间
     * （格式：yyyy-MM-dd HH:mm:ss）
     */
    private LocalDateTime alertTime;
    /**
     * 目标字段
     */
    private String targetField;
    /**
     * 目标值
     */
    private String targetValue;
    /**
     * 累计事件值组
     * （key为事件字段，value为事件累计值）
     */
    private Map<String, Long> eventValueGroup;
}
