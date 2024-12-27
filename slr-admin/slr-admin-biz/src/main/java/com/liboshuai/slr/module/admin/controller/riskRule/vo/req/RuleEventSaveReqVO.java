package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleEventSaveReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @NotBlank(message = "key编号[keyCode]，不能为空")
    @Size(max = 64, message = "key编号[keyCode]，长度不能超过 64 个字符")
    @Schema(description = "key编号", example = "GAME_userId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyCode;

    @NotBlank(message = "事件编号[eventCode]，不能为空")
    @Size(max = 64, message = "事件编号[eventCode]，长度不能超过 64 个字符")
    @Schema(description = "事件编号", example = "GAME_LOTTERY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventCode;

    @NotBlank(message = "事件名称[eventName]，不能为空")
    @Size(max = 64, message = "事件名称[eventName]，长度不能超过 64 个字符")
    @Schema(description = "事件名称", example = "游戏抽奖", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventName;

    @NotBlank(message = "事件描述[eventDesc]，不能为空")
    @Size(max = 64, message = "事件描述[eventDesc]，长度不能超过 64 个字符")
    @Schema(description = "事件描述", example = "游戏平台的抽奖事件", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventDesc;

    @Schema(description = "事件属性组")
    private List<RuleEventAttrSaveRespVO> ruleEventAttrSaveRespVOList;
}
