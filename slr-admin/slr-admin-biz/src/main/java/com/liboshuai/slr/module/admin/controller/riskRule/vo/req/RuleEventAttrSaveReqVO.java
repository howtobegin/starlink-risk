package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleEventAttrSaveReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "属性编号[attributeCode]，不能为空")
    @Schema(description = "属性编号", example = "A175928847299117063")
    private String attributeCode;

    @NotNull(message = "属性值[ruleEventAttrValueSaveReqVO]，不能为空")
    @Schema(description = "属性值")
    private RuleEventAttrValueSaveReqVO ruleEventAttrValueSaveReqVO;
}
