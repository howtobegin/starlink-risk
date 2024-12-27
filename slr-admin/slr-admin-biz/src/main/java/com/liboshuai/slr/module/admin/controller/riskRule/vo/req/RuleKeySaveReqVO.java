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
public class RuleKeySaveReqVO implements Serializable {
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

    @NotBlank(message = "key编号[keyCode]，不能为空")
    @Size(max = 64, message = "key编号[keyCode]，长度不能超过 64 个字符")
    @Schema(description = "key编号", example = "GAME_userId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyCode;

    @NotBlank(message = "key名称[keyName]，不能为空")
    @Size(max = 64, message = "key名称[keyName]，长度不能超过 64 个字符")
    @Schema(description = "key名称", example = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyName;

    @NotBlank(message = "key描述[keyDesc]，不能为空")
    @Size(max = 64, message = "key描述[keyDesc]，长度不能超过 64 个字符")
    @Schema(description = "key描述", example = "游戏平台的用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyDesc;

    @NotEmpty(message = "规则事件组[ruleEventSaveReqVOList]，不能为空")
    @Schema(description = "规则事件组")
    private List<RuleEventSaveReqVO> ruleEventSaveReqVOList;
}
