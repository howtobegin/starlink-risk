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
 * 上送事件Doris DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DorisEventDTO implements Serializable {

    private static final long serialVersionUID = -3125924174631531244L;

    /**
     * 事件时间
     * （无需业务方传输，而是取flink处理时间，格式：yyyy-MM-dd HH:mm:ss）
     */
    private String eventTime;
    /**
     * 目标编号
     * （例如：GAME_userId）
     * {@link RuleInfoDTO#getTargetCode()}
     */
    private String targetCode;
    /**
     * 目标值
     * （例如：U127944222222）
     */
    private String targetValue;
    /**
     * 事件编号
     * （例如：GAME_userId_lottery）
     * {@link RuleCondDTO#getEventCode()}
     */
    private String eventCode;
    /**
     * 事件值
     * （例如：1）
     */
    private String eventValue;
    /**
     * 属性
     */
    private Map<String, String> eventAttrMap;
    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
}
