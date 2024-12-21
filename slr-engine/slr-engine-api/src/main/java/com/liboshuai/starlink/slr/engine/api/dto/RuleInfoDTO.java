package com.liboshuai.starlink.slr.engine.api.dto;

import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 规则信息
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
     */
    private String ruleStatus;
    /**
     * 预警间隔值
     */
    private Long alertIntervalValue;
    /**
     * 预警间隔单位
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
     * 模型信息
     */
    private ModelInfoDTO modelInfoDTO;
    /**
     * 条件组合符
     */
    private String ruleCondCombOp;
    /**
     * 条件组
     */
    private List<RuleCondDTO> ruleCondGroup;
}
