package com.liboshuai.starlink.slr.admin.pojo.vo.riskRule;

import com.liboshuai.starlink.slr.engine.api.enums.ChannelEnum;
import com.liboshuai.starlink.slr.engine.api.enums.RuleCondCombOpEnum;
import com.liboshuai.starlink.slr.engine.api.enums.RuleStatusEnum;
import com.liboshuai.starlink.slr.engine.api.enums.TimeUnitEnum;
import com.liboshuai.starlink.slr.framework.common.pojo.BaseRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class RuleInfoRespVO extends BaseRespVO {

    private static final long serialVersionUID = 1L;

    /**
     * {@link ChannelEnum}
     */
    @Schema(description = "渠道", example = "GAME")
    private String channel;

    @Schema(description = "规则编号", example = "R175928847299117063")
    private String ruleCode;

    @Schema(description = "规则名称", example = "R175928847299117063")
    private String ruleName;

    @Schema(description = "规则描述", example = "游戏低龄高频抽奖规则")
    private String ruleDesc;

    /**
     * {@link RuleStatusEnum}
     */
    @Schema(description = "规则状态", example = "ONLINE")
    private String ruleStatus;

    @Schema(description = "预警间隔值", example = "10")
    private Long alertIntervalValue;

    /**
     * {@link TimeUnitEnum}
     */
    @Schema(description = "预警间隔单位", example = "MINUTE")
    private String alertIntervalUnit;

    @Schema(description = "预警消息", example = "触发xxx预警啦！")
    private String alertMessage;

    @Schema(description = "模型编号", example = "M175928847299117066")
    private String modelCode;

    @Schema(description = "模型信息")
    private RuleModelRespVO ruleModelRespVO;

    /**
     * {@link RuleCondCombOpEnum}
     */
    @Schema(description = "条件组合符", example = "AND")
    private String ruleCondCombOp;

    @Schema(description = "条件组")
    private List<RuleCondRespVO> ruleCondGroup;
}
