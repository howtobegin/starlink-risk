package com.liboshuai.slr.module.admin.api.riskRule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 风控规则目标表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleTargetDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 目标编号
     */
    private String targetCode;

    /**
     * 目标字段
     */
    private String targetField;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 目标描述
     */
    private String targetDesc;

    /**
     * 规则事件组
     */
    private List<RuleEventDTO> ruleEventGroup;
}
