package com.liboshuai.starlink.slr.admin.pojo.entity.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.starlink.slr.engine.api.enums.RuleAuditOpEnum;
import com.liboshuai.starlink.slr.engine.api.enums.RuleEventAttrTypeEnum;
import com.liboshuai.starlink.slr.framework.mybatis.core.dataobject.BaseEntity;
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
