package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.module.engine.enums.RuleEventAttrOpEnum;
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

    @NotEmpty(message = "属性编号[attrCode]，不能为空")
    @Schema(description = "属性编号", example = "GAME_userId_lottery_campaignId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attrCode;

    /**
     * {@link RuleEventAttrOpEnum}
     */
    @NotEmpty(message = "属性比较符[attrOp]，不能为空")
    @Schema(description = "属性比较符", example = "==", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attrOp;

    @NotEmpty(message = "属性值[attrValue]，不能为空")
    @Schema(description = "属性值", example = "C000000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attrValue;

    /**
     * {@link RuleCondSaveReqVO#getCondCode()}
     */
    @NotEmpty(message = "条件编号[condCode]，不能为空")
    @Schema(description = "条件编号", example = "R1553673459123456000_GAME_userId_lottery", requiredMode = Schema.RequiredMode.REQUIRED)
    private String condCode;
}
