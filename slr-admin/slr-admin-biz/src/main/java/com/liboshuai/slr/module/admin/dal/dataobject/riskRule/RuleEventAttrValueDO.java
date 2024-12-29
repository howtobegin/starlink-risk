package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrOpEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则事件属性值表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_event_attr_value")
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrValueDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 属性编号
     * （例如：GAME_userId_lottery_campaignId）
     * {@link RuleEventAttrDO#getAttrCode()}
     */
    private String attrCode;
    /**
     * 属性比较符
     * （例如：==）
     * {@link RuleEventAttrOpEnum}
     */
    private String attrOp;
    /**
     * 属性值
     * （例如：C000000001）
     */
    private String attrValue;
    /**
     * 条件编号
     * （例如：R1553673459123456000_GAME_userId_lottery）
     * {@link RuleCondDO#getCondCode()}
     */
    private String condCode;
}
