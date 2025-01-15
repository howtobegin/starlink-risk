package com.liboshuai.slr.module.admin.api.riskRule.dto;

import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 事件信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleEventApiDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 目标编号
     * {@link RuleTargetApiDTO#getTargetCode()}
     */
    private String targetCode;

    /**
     * 事件编号
     */
    private String eventCode;

    /**
     * 事件字段
     */
    private String eventField;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件描述
     */
    private String eventDesc;

    /**
     * 事件状态
     * {@link CommonStatusEnum}
     */
    private String eventStatus;

    /**
     * 事件属性组
     */
    private List<RuleEventAttrApiDTO> ruleEventAttrGroup;
}
