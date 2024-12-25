package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.module.engine.enums.ChannelEnum;
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
     * （无需业务方传输，而是取到达flink的时间，格式为：yyyy-MM-dd HH:mm:ss）
     */
    private String eventTime;
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
