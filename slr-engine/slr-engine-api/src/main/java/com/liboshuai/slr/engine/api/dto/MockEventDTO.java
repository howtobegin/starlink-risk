package com.liboshuai.slr.engine.api.dto;

import com.liboshuai.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.slr.engine.api.type.MockEventDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * 上送事件Nginx DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(MockEventDtoType.class)
public class MockEventDTO implements Serializable {

    private static final long serialVersionUID = -3125924174631531244L;

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
    private Map<String, String> eventAttrMap;
}
