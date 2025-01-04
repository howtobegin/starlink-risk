package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则模型表
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
     * （例如：1553673459123456001）
     */
    private Long modelCode;
    /**
     * 模型名称
     */
    private String modelName;
    /**
     * 模型描述
     */
    private String modelDesc;
    /**
     * 运算机groovy代码
     */
    private String groovy;
}
