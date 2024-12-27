package com.liboshuai.slr.module.admin.controller.riskRule.vo;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则目标表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleKeyRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    /**
     * {@link ChannelEnum}
     */
    @Schema(description = "渠道", example = "GAME")
    private String channel;

    @Schema(description = "key编号", example = "GAME_userId")
    private String keyCode;

    @Schema(description = "key名称", example = "用户id")
    private String keyName;

    @Schema(description = "key描述", example = "游戏平台的用户ID")
    private String keyDesc;
}
