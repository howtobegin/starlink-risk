package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.framework.common.pojo.BaseDTO;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import com.liboshuai.slr.module.engine.enums.RuleCondCombOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 风控规则信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleInfoDTO extends BaseDTO {

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
     * 规则目标
     * {@link RuleKeyDTO#getKeyCode()}
     */
    private String keyCode;

    /**
     * 规则目标信息
     */
    private RuleKeyDTO ruleKeyDTO;

    /**
     * 模型编号
     */
    private String modelCode;

    /**
     * 模型信息
     */
    private RuleModelDTO ruleModelDTO;

    /**
     * 条件组合符
     * {@link RuleCondCombOpEnum}
     */
    private String ruleCondCombOp;

    /**
     * 条件组
     */
    private List<RuleCondDTO> ruleCondDTOList;
}
