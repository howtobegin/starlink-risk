package com.liboshuai.starlink.slr.engine.api.dto;

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
public class RuleEventAttributeDTO extends BaseDTO {
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
     * 属性key
     */
    private String attributeKey;
    /**
     * 属性值
     */
    private String attributeValue;
}
