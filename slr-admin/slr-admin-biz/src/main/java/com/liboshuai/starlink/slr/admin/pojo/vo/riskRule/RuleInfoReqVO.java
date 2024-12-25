package com.liboshuai.starlink.slr.admin.pojo.vo.riskRule;

import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.starlink.slr.engine.api.enums.RuleStatusEnum;
import com.liboshuai.starlink.slr.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 规则信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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
