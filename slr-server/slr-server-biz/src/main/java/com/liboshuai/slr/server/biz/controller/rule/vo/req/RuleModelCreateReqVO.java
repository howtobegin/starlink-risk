package com.liboshuai.slr.server.biz.controller.rule.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 风控规则模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleModelCreateReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "模型名称[modelName]，不能为空")
    @Schema(description = "模型名称", example = "模型运算机一", requiredMode = Schema.RequiredMode.REQUIRED)
    private String modelName;

    @NotBlank(message = "模型描述[modelDesc]，不能为空")
    @Schema(description = "模型描述", example = "支持周期条件的规则模型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String modelDesc;

    @NotBlank(message = "运算机groovy代码[groovy]，不能为空")
    @Schema(description = "运算机groovy代码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String groovy;
}
