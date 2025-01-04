package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import com.liboshuai.slr.module.engine.enums.RuleCondCombOpEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 风控规则信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleInfoDTO implements Serializable {

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
     * （例如：游戏高频抽奖）
     */
    private String ruleName;

    /**
     * 规则描述
     * （例如：游戏高频抽奖规则）
     */
    private String ruleDesc;

    /**
     * 规则状态
     * {@link CommonStatusEnum}
     */
    private String ruleStatus;

    /**
     * 版本
     */
    private Long version;

    /**
     * 预警间隔值
     * （例如：10）
     */
    private Long alertIntervalValue;

    /**
     * 预警间隔单位
     * {@link TimeUnitEnum}
     */
    private String alertIntervalUnit;

    /**
     * 预警消息
     * （例如：触发xxx预警啦！）
     */
    private String alertMessage;

    /**
     * 目标编号
     * （例如：GAME_userId）
     */
    private String targetCode;

    /**
     * 目标字段
     * （例如：userId）
     */
    private String targetField;

    /**
     * 目标名称
     * （例如：用户id）
     */
    private String targetName;

    /**
     * 模型编号
     */
    private Long modelCode;

    /**
     * 模型groovy代码
     */
    private String modelGroovy;

    /**
     * 条件组合符
     * {@link RuleCondCombOpEnum}
     */
    private String ruleCondCombOp;

    /**
     * 条件组
     */
    private List<RuleCondDTO> ruleCondGroup;
}
