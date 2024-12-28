package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.framework.common.pojo.BaseDTO;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则事件属性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 事件编号
     * {@link RuleEventDTO#getEventCode()}
     */
    private String eventCode;

    /**
     * 属性编号
     */
    private String attributeCode;

    /**
     * 属性名称
     */
    private String attributeName;

    /**
     * 属性key
     */
    private String attributeKey;

    /**
     * 属性类型
     * {@link RuleEventAttrTypeEnum}
     */
    private String attributeType;

    /**
     * 属性值
     */
    private RuleEventAttrValueDTO ruleEventAttrValueDTO;
}
