package com.liboshuai.starlink.slr.engine.api.dto;

import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 上送事件Kafka DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KafkaEventDTO implements Serializable {

    private static final long serialVersionUID = -3125924174631531244L;

    /**
     * 事件时间
     * （无需业务方传输，仅用于引擎计算处理使用）
     */
    private long eventTimestamp;
    /**
     * key编号
     */
    private String keyCode;
    /**
     * key值
     */
    private String keyValue;
    /**
     * 事件编号
     */
    private String eventCode;
    /**
     * 事件值
     */
    private String eventValue;
    /**
     * 属性
     * {@link RuleEventAttrDTO}
     */
    private Map<String, String> eventAttribute;
    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
}
