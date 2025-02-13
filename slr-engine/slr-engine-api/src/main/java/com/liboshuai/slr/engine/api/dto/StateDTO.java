package com.liboshuai.slr.engine.api.dto;

import com.liboshuai.slr.engine.api.type.StateHistoryDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;


/**
 * 规则状态历史的记录数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(StateHistoryDtoType.class)
public class StateDTO implements Serializable {

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
