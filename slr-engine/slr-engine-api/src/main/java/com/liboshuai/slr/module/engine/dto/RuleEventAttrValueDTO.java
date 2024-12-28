package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.framework.common.pojo.BaseDTO;
import com.liboshuai.slr.module.engine.enums.RuleAuditOpEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则事件属性值
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrValueDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 条件编号
     * {@link RuleCondDTO#getCondCode()}
     */
    private String condCode;

    /**
     * 属性编号
     * {@link RuleEventAttrDTO#getAttributeCode()}
     */
    private String attributeCode;

    /**
     * 属性值
     */
    private String attributeValue;

    /**
     * 属性比较符
     * {@link RuleAuditOpEnum}
     */
    private String attributeOp;
}
