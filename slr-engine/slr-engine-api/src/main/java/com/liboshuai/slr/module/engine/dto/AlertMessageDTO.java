package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import com.liboshuai.slr.module.engine.type.AlertMessageDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * 预警信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(AlertMessageDtoType.class)
public class AlertMessageDTO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
    /**
     * 规则编号
     * {@link RuleInfoDTO#getRuleCode()}
     */
    private Long ruleCode;
    /**
     * 事件id
     */
    private Long eventId;
    /**
     * 预警消息
     */
    private String alertMessage;
    /**
     * 预警时间
     * （格式：yyyy-MM-dd HH:mm:ss）
     * 注意不要使用 LocalDateTime，这样无法使用 POJO 序列化，严重降低性能
     */
    private String alertTime;
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
