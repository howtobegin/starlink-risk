package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.validation.InStringEnum;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleKeyDO;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import com.liboshuai.slr.module.engine.enums.RuleCondCombOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleInfoSaveReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * {@link ChannelEnum}
     */
    @NotBlank(message = "渠道[channel]，不能为空")
    @InStringEnum(value = ChannelEnum.class, message = "渠道[channel]，必须在指定范围 {value}")
    @Schema(description = "渠道", example = "GAME")
    private String channel;

    @Size(max = 20, message = "规则名称[ruleName]，长度不能超过 20 个字符")
    @NotBlank(message = "规则名称[ruleName]，不能为空")
    @Schema(description = "规则名称", example = "游戏高频抽奖")
    private String ruleName;

    @Size(max = 64, message = "规则描述[ruleDesc]，长度不能超过 64 个字符")
    @NotBlank(message = "规则描述[ruleDesc]，不能为空")
    @Schema(description = "规则描述", example = "游戏低龄高频抽奖规则")
    private String ruleDesc;

    /**
     * {@link RuleStatusEnum}
     */
    @NotBlank(message = "规则状态[ruleStatus]，不能为空")
    @InStringEnum(value = RuleStatusEnum.class, message = "规则状态[ruleStatus]，必须在指定范围 {value}")
    @Schema(description = "规则状态", example = "ONLINE")
    private String ruleStatus;

    @NotNull(message = "预警间隔值[alertIntervalValue]，不能为空")
    @Schema(description = "预警间隔值", example = "10")
    private Long alertIntervalValue;

    /**
     * {@link TimeUnitEnum}
     */
    @NotBlank(message = "预警间隔单位[alertIntervalUnit]，不能为空")
    @InStringEnum(value = TimeUnitEnum.class, message = "预警间隔单位[alertIntervalUnit]，必须在指定范围 {value}")
    @Schema(description = "预警间隔单位", example = "MINUTE")
    private String alertIntervalUnit;

    @NotBlank(message = "预警消息[alertMessage]，不能为空")
    @Schema(description = "预警消息", example = "触发xxx预警啦！")
    private String alertMessage;

    /**
     * {@link RuleKeyDO#getKeyCode()}
     */
    @NotBlank(message = "规则目标[keyCode]，不能为空")
    @Schema(description = "规则目标", example = "GAME_userId")
    private String keyCode;

    @NotBlank
    @NotBlank(message = "模型编号[modelCode]，不能为空")
    @Schema(description = "模型编号", example = "M175928847299117063")
    private String modelCode;

    /**
     * {@link RuleCondCombOpEnum}
     */
    @NotBlank(message = "条件组合符[ruleCondCombOp]，不能为空")
    @InStringEnum(value = RuleCondCombOpEnum.class, message = "条件组合符[ruleCondCombOp]，必须在指定范围 {value}")
    @Schema(description = "条件组合符", example = "AND")
    private String ruleCondCombOp;

    @NotNull(message = "条件组[ruleCondSaveReqVOList]，不能为空")
    @Schema(description = "条件组")
    private List<RuleCondSaveReqVO> ruleCondSaveReqVOList;
}
