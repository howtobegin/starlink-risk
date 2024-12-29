package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.validation.InStringEnum;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleTargetSaveReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    /**
     * {@link ChannelEnum}
     */
    @NotBlank(message = "渠道[channel]，不能为空")
    @InStringEnum(value = ChannelEnum.class, message = "渠道[channel]，必须在指定范围 {value}")
    @Schema(description = "渠道", example = "GAME", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channel;

    @NotBlank(message = "目标编号[targetCode]，不能为空")
    @Size(max = 256, message = "目标编号[targetCode]，长度不能超过 256 个字符")
    @Schema(description = "目标编号", example = "GAME_userId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetCode;

    @NotBlank(message = "目标名称[targetName]，不能为空")
    @Size(max = 32, message = "key名称[targetName]，长度不能超过 32 个字符")
    @Schema(description = "目标名称", example = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetName;

    @NotBlank(message = "目标描述[targetDesc]，不能为空")
    @Size(max = 64, message = "目标描述[targetDesc]，长度不能超过 64 个字符")
    @Schema(description = "目标描述", example = "游戏平台的用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetDesc;

    @NotEmpty(message = "规则事件组[ruleEventSaveGroup]，不能为空")
    @Schema(description = "规则事件组")
    private List<RuleEventSaveReqVO> ruleEventSaveGroup;
}
