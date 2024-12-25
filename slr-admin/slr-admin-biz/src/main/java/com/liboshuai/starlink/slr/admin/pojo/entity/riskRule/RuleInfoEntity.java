package com.liboshuai.starlink.slr.admin.pojo.entity.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.starlink.slr.engine.api.enums.RuleCondCombOpEnum;
import com.liboshuai.starlink.slr.engine.api.enums.RuleStatusEnum;
import com.liboshuai.starlink.slr.engine.api.enums.TimeUnitEnum;
import com.liboshuai.starlink.slr.framework.mybatis.core.dataobject.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 规则信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_info")
@EqualsAndHashCode(callSuper = true)
public class RuleInfoEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 渠道
     * {@link ChannelEnum}
     */
    private String channel;
    /**
     * 规则编号
     */
    private String ruleCode;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 规则描述
     */
    private String ruleDesc;
    /**
     * 规则状态
     * {@link RuleStatusEnum}
     */
    private String ruleStatus;
    /**
     * 预警间隔值
     */
    private Long alertIntervalValue;
    /**
     * 预警间隔单位
     * {@link TimeUnitEnum}
     */
    private String alertIntervalUnit;
    /**
     * 预警消息
     */
    private String alertMessage;
    /**
     * 模型编号
     */
    private String modelCode;
    /**
     * 条件组合符
     * {@link RuleCondCombOpEnum}
     */
    private String ruleCondCombOp;

}
