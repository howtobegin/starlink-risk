package com.liboshuai.slr.module.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * 记录规则状态的key历史记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleKeyHistoryDTO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 规则编号
     */
    private Long ruleCode;

    /**
     * 规则版本
     */
    private Long ruleVersion;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 目标字段
     */
    private String targetField;

    /**
     * 目标值
     */
    private String targetValue;
}
