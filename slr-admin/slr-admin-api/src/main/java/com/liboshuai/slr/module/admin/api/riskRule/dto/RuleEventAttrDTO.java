package com.liboshuai.slr.module.admin.api.riskRule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 风控规则事件属性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleEventAttrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 属性编号
     */
    private String attrCode;

    /**
     * 属性字段
     */
    private String attrField;

    /**
     * 属性名称
     */
    private String attrName;

    /**
     * 属性类型
     */
    private String attrType;

    /**
     * 事件编号
     * {@link RuleEventAttrDTO#getEventCode()}
     */
    private String eventCode;

}
