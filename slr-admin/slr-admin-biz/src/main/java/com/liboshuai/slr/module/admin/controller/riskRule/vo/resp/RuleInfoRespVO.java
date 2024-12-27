package com.liboshuai.slr.module.admin.controller.riskRule.vo.resp;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.admin.dal.dataobject.riskRule.RuleKeyDO;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import com.liboshuai.slr.module.engine.enums.RuleCondCombOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleStatusEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class RuleInfoRespVO extends BaseRespVO {

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

    /**
     * {@link RuleKeyDO#getKeyCode()}
     */
    @Schema(description = "规则目标", example = "GAME_userId")
    private String keyCode;

    @Schema(description = "规则目标信息")
    private RuleKeyRespVO ruleKeyRespVO;

    @Schema(description = "模型编号", example = "M175928847299117063")
    private String modelCode;

    @Schema(description = "模型信息")
    private RuleModelRespVO ruleModelRespVO;

    /**
     * {@link RuleCondCombOpEnum}
     */
    @Schema(description = "条件组合符", example = "AND")
    private String ruleCondCombOp;

    @Schema(description = "条件组")
    private List<RuleCondRespVO> ruleCondRespVoList;
}
