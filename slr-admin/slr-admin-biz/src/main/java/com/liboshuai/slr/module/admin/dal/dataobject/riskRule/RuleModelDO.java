package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
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
@TableName("slr_rule_model")
@EqualsAndHashCode(callSuper = true)
public class RuleModelDO extends BaseDO {
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
     * {@link RuleStatusEnum}
     */
    private String groovy;
}
