package com.liboshuai.starlink.slr.engine.api.dto;

import com.liboshuai.starlink.slr.engine.api.enums.RuleCondAttributeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 事件属性组
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAttributeDTO implements Serializable {
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
