package com.liboshuai.slr.module.admin.controller.riskRule.vo.resp;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 风控规则目标表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleTargetRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    /**
     * {@link ChannelEnum}
     */
    @Schema(description = "渠道", example = "GAME")
    private String channel;

    @Schema(description = "目标编号", example = "GAME_userId")
    private String targetCode;

    @Schema(description = "目标字段", example = "userId")
    private String targetFiled;

    @Schema(description = "目标名称", example = "用户id")
    private String targetName;

    @Schema(description = "目标描述", example = "游戏平台的用户ID")
    private String targetDesc;

    @Schema(description = "规则事件组")
    private List<RuleEventRespVO> ruleEventGroup;
}
