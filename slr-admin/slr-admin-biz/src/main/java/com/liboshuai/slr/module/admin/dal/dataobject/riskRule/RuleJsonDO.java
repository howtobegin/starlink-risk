package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则json表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_json")
@EqualsAndHashCode(callSuper = true)
public class RuleJsonDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 规则编号
     * （例如：R1553673459123456000）
     * {@link RuleInfoDO#getRuleCode()}
     */
    private String ruleCode;

    /**
     * 规则json
     */
    private String ruleJson;

}
