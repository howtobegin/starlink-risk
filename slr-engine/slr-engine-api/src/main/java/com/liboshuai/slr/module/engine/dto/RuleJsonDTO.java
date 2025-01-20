package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.module.engine.type.RuleJsonDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;

/**
 * 规则json字符串DTO对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(RuleJsonDtoType.class)
public class RuleJsonDTO implements Serializable {

    private static final long serialVersionUID = -6940398101611093673L;

    /**
     * 规则编号
     * （例如：R1553673459123456000）
     * {@link RuleInfoDTO#getRuleCode()}
     */
    private Long ruleCode;

    /**
     * 规则json数据
     */
    private String ruleJson;

}
