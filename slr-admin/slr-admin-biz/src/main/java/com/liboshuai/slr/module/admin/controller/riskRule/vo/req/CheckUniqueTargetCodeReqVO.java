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
public class CheckUniqueTargetCodeReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "目标ID[targetId]，不能为空")
    @Schema(description = "目标ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetId;

    @NotBlank(message = "目标编号[targetCode]，不能为空")
    @Schema(description = "目标编号", example = "GAME_userId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetCode;

}
