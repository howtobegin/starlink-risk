package com.liboshuai.slr.server.biz.controller.rule.vo.resp;

import com.liboshuai.slr.engine.api.enums.*;
import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
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
    @Schema(description = "渠道", example = "game")
    private String channel;

    @Schema(description = "规则编号", example = "1553673459123456000")
    private Long ruleCode;

    @Schema(description = "规则名称", example = "游戏高频抽奖")
    private String ruleName;

    @Schema(description = "规则描述", example = "游戏高频抽奖规则")
    private String ruleDesc;

    /**
     * {@link CommonStatusEnum}
     */
    @Schema(description = "规则状态", example = "ONLINE")
    private String ruleStatus;

    @Schema(description = "版本", example = "1")
    private Long ruleVersion;

    @Schema(description = "预警间隔值", example = "10")
    private Long alertIntervalValue;

    /**
     * {@link TimeUnitEnum}
     */
    @Schema(description = "预警间隔单位", example = "分钟")
    private String alertIntervalUnit;

    /**
     * {@link AlertProjectNoEnum}
     */
    @Schema(description = "预警项目编号", example = "game")
    private String alertProjectNo;

    /**
     * {@link AlertLevelEnum}
     */
    @Schema(description = "预警等级", example = "4")
    private String alertLevel;

    @Schema(description = "预警模板", example = "触发xxx预警啦！")
    private String alertTemplate;

    /**
     * {@link RuleTargetRespVO#getTargetCode()}
     */
    @Schema(description = "目标编号", example = "game_userId")
    private String targetCode;

    @Schema(description = "目标字段", example = "userId")
    private String targetField;

    @Schema(description = "目标名称", example = "用户id")
    private String targetName;

    /**
     * {@link RuleModelRespVO#getModelCode()}
     */
    @Schema(description = "模型编号", example = "1553673459123456001")
    private Long modelCode;

    @Schema(description = "模型名称", example = "模型运算机一")
    private String modelName;

    /**
     * {@link RuleCondCombOpEnum}
     */
    @Schema(description = "条件组合符", example = "AND")
    private String ruleCondCombOp;

    @Schema(description = "条件组")
    private List<RuleCondRespVO> ruleCondGroup;
}
