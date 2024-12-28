package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.framework.common.pojo.BaseDTO;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleModelDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 模型编号
     */
    private String modelCode;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型描述
     */
    private String modelDesc;

    /**
     * 模型状态
     * {@link RuleStatusEnum}
     */
    private String modelStatus;

    /**
     * 运算机groovy代码
     */
    private String groovy;
}
