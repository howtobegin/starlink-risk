package com.liboshuai.slr.server.biz.dal.dataobject.rule;

import com.baomidou.mybatisplus.annotation.TableName;
import com.liboshuai.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.slr.engine.api.enums.RuleCondCombOpEnum;
import com.liboshuai.slr.engine.api.enums.TimeUnitEnum;
import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.mybatis.core.dataobject.BaseDO;
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
     * （例如：1553673459123456000）
     */
    private Long ruleCode;
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
     * {@link CommonStatusEnum}
     */
    private String ruleStatus;
    /**
     * 规则版本
     */
    private Long ruleVersion;
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
     * 预警项目编号
     */
    private String alertProjectNo;
    /**
     * 预警等级
     */
    private String alertLevel;
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
     * 规则目标
     * （例如：GAME_userId）
     * {@link RuleTargetDO#getTargetCode()}
     */
    private String targetCode;
    /**
     * 模型编号
     * （例如：M1553673459123456001）
     * {@link RuleModelDO#getModelCode()}
     */
    private Long modelCode;

}
