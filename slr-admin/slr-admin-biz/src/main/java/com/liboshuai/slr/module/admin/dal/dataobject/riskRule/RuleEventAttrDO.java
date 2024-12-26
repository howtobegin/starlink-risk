package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则事件属性表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_event_attr")
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrDO extends BaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 事件编号
     * {@link RuleEventDO#getEventCode()}
     */
    private String eventCode;
    /**
     * 属性编号
     */
    private String attributeCode;
    /**
     * 属性名称
     * （例如：订单号）
     */
    private String attributeName;
    /**
     * 属性key
     * （例如：orderNo）
     */
    private String attributeKey;
    /**
     * 属性类型
     * （例如：String）
     * {@link RuleEventAttrTypeEnum}
     */
    private String attributeType;
}
