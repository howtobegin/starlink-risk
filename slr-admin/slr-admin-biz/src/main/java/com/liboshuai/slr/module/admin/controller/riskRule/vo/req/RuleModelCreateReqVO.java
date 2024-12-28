package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 风控规则模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleModelCreateReqVO extends PageParam {
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
