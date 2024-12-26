package com.liboshuai.slr.module.admin.dal.dataobject.riskRule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import com.liboshuai.slr.module.engine.enums.RuleCondCombOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则信息表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("slr_rule_info")
@EqualsAndHashCode(callSuper = true)
public class RuleInfoDO extends BaseDO {

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
     * 条件组合符
     * {@link RuleCondCombOpEnum}
     */
    private String ruleCondCombOp;
    /**
     * 模型编号
     * {@link RuleModelDO#getModelCode()}
     */
    private String modelCode;

}
