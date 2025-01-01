package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.pojo.PageParam;
import com.liboshuai.slr.framework.common.validation.InStringEnum;
import com.liboshuai.slr.module.engine.enums.ChannelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 分页风控目标信息 Request VO")
public class RuleTargetPageReqVO extends PageParam {

    private static final long serialVersionUID = 1L;

    /**
     * {@link ChannelEnum}
     */
    @NotBlank(message = "渠道[channel]，不能为空")
    @InStringEnum(value = ChannelEnum.class, message = "渠道[channel]，必须在指定范围 {value}")
    @Schema(description = "渠道", example = "GAME")
    private String channel;

    @Schema(description = "目标编号", example = "GAME_userId")
    private String targetCode;

    @Schema(description = "目标字段", example = "userId")
    private String targetField;

    @Schema(description = "目标名称", example = "用户id")
    private String targetName;

    @Schema(description = "目标描述", example = "游戏平台的用户ID")
    private String targetDesc;

}
