package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.module.engine.type.RuleKeyHistoryDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;


/**
 * 记录规则状态的key历史记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(RuleKeyHistoryDtoType.class)
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
