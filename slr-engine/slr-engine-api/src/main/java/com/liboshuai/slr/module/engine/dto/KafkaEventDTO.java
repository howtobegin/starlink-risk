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
     * （无需业务方传输，而是取flink处理时间）
     */
    private Long eventTime;
    /**
     * 目标编号
     * （例如：userId）
     * {@link RuleInfoDTO#getTargetField()}
     */
    private String targetField;
    /**
     * 目标值
     * （例如：U127944222222）
     */
    private String targetValue;
    /**
     * 事件编号
     * （例如：lottery）
     * {@link RuleCondDTO#getEventField()}
     */
    private String eventField;
    /**
     * 事件值
     * （例如：1）
     */
    private String eventValue;
    /**
     * 属性
     * key关联{@link RuleEventAttrValueDTO#getAttrField()}
     */
    private Map<String, String> eventAttrMap;
    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
}
