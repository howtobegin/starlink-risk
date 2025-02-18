package com.liboshuai.slr.server.biz.dal.dataobject.rule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.engine.api.enums.RuleEventAttrTypeEnum;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
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
     * 属性编号
     * （例如：game_userId_lottery_campaignId）
     */
    private String attrCode;
    /**
     * 属性字段
     * （例如：campaignId）
     */
    private String attrField;
    /**
     * 属性名称
     * （例如：活动id）
     */
    private String attrName;
    /**
     * 属性类型
     * （例如：String）
     * {@link RuleEventAttrTypeEnum}
     */
    private String attrType;
    /**
     * 事件编号
     * （例如：game_userId_lottery）
     * {@link RuleEventDO#getEventCode()}
     */
    private String eventCode;
}
