package com.liboshuai.slr.module.connector.api.alertMessage.dto;

import com.liboshuai.slr.framework.common.pojo.PageParam;
import lombok.*;
import lombok.experimental.Accessors;

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
public class AlertMessageReqDTO extends PageParam {

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
     * 预警时间开始
     */
    private LocalDateTime alertTimeStart;
    /**
     * 预警时间结束
     */
    private LocalDateTime alertTimeEnd;
}
