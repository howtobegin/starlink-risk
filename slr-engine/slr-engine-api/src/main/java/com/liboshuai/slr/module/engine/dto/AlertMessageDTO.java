package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
     * {@link ChannelEnum}
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
     * （格式：yyyy-MM-dd HH:mm:ss）
     */
    private String alertTime;
}
