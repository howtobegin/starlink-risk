package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.module.engine.enums.RuleAuditOpEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleEventAttrValueSaveReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "条件编号（无需前端传入）", example = "A175928847299117063")
    private String condCode;

    @NotEmpty(message = "属性编号[attributeCode]，不能为空")
    @Schema(description = "属性编号", example = "A175928847299117063")
    private String attributeCode;

    @NotEmpty(message = "属性值[attributeValue]，不能为空")
    @Schema(description = "属性值", example = "C175928847299117065")
    private String attributeValue;
    /**
     * {@link RuleAuditOpEnum}
     */
    @NotEmpty(message = "属性比较符[attributeOp]，不能为空")
    @Schema(description = "属性比较符", example = "C175928847299117065")
    private String attributeOp;
}
