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
public class CheckUniqueKeyCodeReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "目标ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long keyId;

    @NotBlank(message = "目标编号[keyCode]，不能为空")
    @Schema(description = "目标编号", example = "GAME_userId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyCode;

}
