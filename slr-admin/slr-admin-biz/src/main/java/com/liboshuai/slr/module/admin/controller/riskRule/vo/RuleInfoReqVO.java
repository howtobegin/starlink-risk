package com.liboshuai.slr.module.admin.controller.riskRule.vo;

import com.liboshuai.slr.framework.common.pojo.PageParam;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
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
@EqualsAndHashCode(callSuper = true)
public class RuleInfoReqVO extends PageParam {

    private static final long serialVersionUID = 1L;

    /**
     * {@link ChannelEnum}
     */
    @Schema(description = "渠道", example = "GAME")
    private String channel;

    @Schema(description = "规则编号", example = "R175928847299117063")
    private String ruleCode;

    @Schema(description = "规则名称", example = "游戏高频抽奖")
    private String ruleName;

    /**
     * {@link RuleStatusEnum}
     */
    @Schema(description = "规则状态", example = "ONLINE")
    private String ruleStatus;

    @Schema(description = "模型编号", example = "M175928847299117066")
    private String modelCode;

}
