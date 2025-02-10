package com.liboshuai.slr.engine.api.dto;

import com.liboshuai.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.slr.engine.api.type.KafkaEventDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;

/**
 * 上送事件Nginx DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(KafkaEventDtoType.class)
public class NginxEventDTO implements Serializable {

    private static final long serialVersionUID = -3125924174631531244L;

    /**
     * 事件时间（例如：1736732339769）
     * 13位，毫秒级
     * 第一版本使用flink处理时间，暂不使用业务平传输的时间
     */
    private String eventTime;
    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
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
    private String eventAttrMap;
}
