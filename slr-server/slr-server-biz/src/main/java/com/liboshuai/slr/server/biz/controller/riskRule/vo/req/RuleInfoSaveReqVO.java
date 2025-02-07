package com.liboshuai.slr.server.biz.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.common.validation.InStringEnum;
import com.liboshuai.slr.module.engine.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "管理后台 - 新增风控规则信息 Request VO")
public class RuleInfoSaveReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * {@link ChannelEnum}
     */
    @NotBlank(message = "渠道[channel]，不能为空")
    @InStringEnum(value = ChannelEnum.class, message = "渠道[channel]，必须在指定范围 {value}")
    @Schema(description = "渠道", example = "GAME", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channel;

    @Schema(description = "规则编号", example = "175928847299117063", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long ruleCode;

    @Size(max = 32, message = "规则名称[ruleName]，长度不能超过 32 个字符")
    @NotBlank(message = "规则名称[ruleName]，不能为空")
    @Schema(description = "规则名称", example = "游戏高频抽奖", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ruleName;

    @Size(max = 64, message = "规则描述[ruleDesc]，长度不能超过 64 个字符")
    @NotBlank(message = "规则描述[ruleDesc]，不能为空")
    @Schema(description = "规则描述", example = "游戏低龄高频抽奖规则", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ruleDesc;

    /**
     * {@link CommonStatusEnum}
     */
    @InStringEnum(value = CommonStatusEnum.class, message = "规则状态[ruleStatus]，必须在指定范围 {value}")
    @Schema(description = "规则状态", example = "ONLINE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String ruleStatus;

    @Schema(description = "版本", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ruleVersion;

    @Schema(description = "预警间隔值", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long alertIntervalValue;

    /**
     * {@link TimeUnitEnum}
     */
    @InStringEnum(value = TimeUnitEnum.class, message = "预警间隔单位[alertIntervalUnit]，必须在指定范围 {value}")
    @Schema(description = "预警间隔单位", example = "分钟", requiredMode = Schema.RequiredMode.REQUIRED)
    private String alertIntervalUnit;

    /**
     * {@link AlertProjectNoEnum}
     */
    @NotBlank(message = "预警项目编号[alertProjectNo]，不能为空")
    @InStringEnum(value = AlertProjectNoEnum.class, message = "预警项目编号[alertProjectNo]，必须在指定范围 {value}")
    @Schema(description = "预警项目编号", example = "game")
    private String alertProjectNo;

    /**
     * {@link AlertLevelEnum}
     */
    @NotBlank(message = "预警等级[alertLevel]，不能为空")
    @InStringEnum(value = AlertLevelEnum.class, message = "预警等级[alertLevel]，必须在指定范围 {value}")
    @Schema(description = "预警等级", example = "4")
    private String alertLevel;

    @NotBlank(message = "预警消息[alertMessage]，不能为空")
    @Schema(description = "预警消息", example = "触发xxx预警啦！", requiredMode = Schema.RequiredMode.REQUIRED)
    private String alertMessage;

    /**
     * {@link RuleTargetSaveReqVO#getTargetCode()}
     */
    @NotBlank(message = "规则目标[targetCode]，不能为空")
    @Schema(description = "规则目标", example = "GAME_userId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetCode;

    @NotNull(message = "模型编号[modelCode]，不能为空")
    @Schema(description = "模型编号", example = "1553673459123456001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long modelCode;

    /**
     * {@link RuleCondCombOpEnum}
     */
    @InStringEnum(value = RuleCondCombOpEnum.class, message = "条件组合符[ruleCondCombOp]，必须在指定范围 {value}")
    @Schema(description = "条件组合符", example = "AND", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ruleCondCombOp;

    @Valid
    @NotEmpty(message = "条件组[ruleCondGroup]，不能为空")
    @Schema(description = "条件组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RuleCondSaveReqVO> ruleCondGroup;
}
