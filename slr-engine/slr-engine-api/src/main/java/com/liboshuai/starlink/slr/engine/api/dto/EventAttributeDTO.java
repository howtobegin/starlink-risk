package com.liboshuai.starlink.slr.engine.api.dto;

import com.liboshuai.starlink.slr.engine.api.enums.RuleCondAttributeTypeEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 事件属性组
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class EventAttributeDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 事件编号
     */
    private String eventCode;
    /**
     * 属性名称
     */
    private String attributeName;
    /**
     * 属性类型
     * {@link RuleCondAttributeTypeEnum}
     */
    private String attributeType;
    /**
     * 属性描述
     */
    private String attributeDesc;
}
