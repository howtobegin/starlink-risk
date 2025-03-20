package com.liboshuai.slr.server.biz.controller.rule.vo.req;

import com.liboshuai.slr.engine.api.enums.RuleEventAttrOpEnum;
import com.liboshuai.slr.framework.common.validation.InStringEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Size;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleEventAttrValueSaveReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = 256, message = "属性编号[attrCode]，长度不能超过 256 个字符")
    @Schema(description = "属性编号", example = "game_userId_lottery_campaignId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attrCode;

    /**
     * {@link RuleEventAttrOpEnum}
     */
    @InStringEnum(value = RuleEventAttrOpEnum.class, message = "属性比较符[attrOp]，必须在指定范围 {value}")
    @Schema(description = "属性比较符", example = "==", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attrOp;

    @Schema(description = "属性值", example = "C000000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attrValue;

    /**
     * {@link RuleCondSaveReqVO#getCondCode()}
     */
    @Size(max = 256, message = "条件编号[condCode]，长度不能超过 256 个字符")
    @Schema(description = "条件编号", example = "R1553673459123456000_game_userId_lottery", requiredMode = Schema.RequiredMode.REQUIRED)
    private String condCode;
}
