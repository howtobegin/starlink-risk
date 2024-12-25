package com.liboshuai.slr.module.admin.pojo.entity.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseEntity;
import com.liboshuai.slr.module.engine.enums.RuleAuditOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 事件属性组
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_event_attr")
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 事件编号
     */
    private String eventCode;
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
    /**
     * 属性值
     * （例如：C000000001）
     */
    private String attributeValue;
    /**
     * 属性比较符
     * （例如：=）
     * {@link RuleAuditOpEnum}
     */
    private String attributeOp;
}
