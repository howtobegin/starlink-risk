package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CheckUniqueEventCodeReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "事件ID[eventId]，不能为空")
    @Schema(description = "事件ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long eventId;

    @NotBlank(message = "事件编号[keyCode]，不能为空")
    @Schema(description = "事件编号", example = "GAME_lottery", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventCode;

}
