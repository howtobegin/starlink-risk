package com.liboshuai.starlink.slr.engine.api.dto;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 规则模型信息
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
     */
    private String modelStatus;
    /**
     * 运算机 groovy 代码
     */
    private String groovy;
}
