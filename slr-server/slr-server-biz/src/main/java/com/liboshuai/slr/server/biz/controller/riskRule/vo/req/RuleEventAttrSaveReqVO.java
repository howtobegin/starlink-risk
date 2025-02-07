package com.liboshuai.slr.server.biz.controller.riskRule.vo.req;

import com.liboshuai.slr.engine.api.enums.RuleEventAttrTypeEnum;
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
public class RuleEventAttrSaveReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = 256, message = "属性编号[attrCode]，长度不能超过 256 个字符")
    @Schema(description = "属性编号", example = "GAME_userId_lottery_campaignId")
    private String attrCode;

    @Size(max = 256, message = "属性字段[attrField]，长度不能超过 256 个字符")
    @Schema(description = "属性字段", example = "campaignId")
    private String attrField;

    @Size(max = 32, message = "属性名称[attrCode]，长度不能超过 32 个字符")
    @Schema(description = "属性名称", example = "活动ID")
    private String attrName;

    /**
     * {@link RuleEventAttrTypeEnum}
     */
    @Schema(description = "属性类型", example = "String")
    private String attrType;

    /**
     * {@link RuleEventSaveReqVO#getEventCode()}
     */
    @Size(max = 256, message = "事件编号[attrCode]，长度不能超过 256 个字符")
    @Schema(description = "事件编号", example = "GAME_userId_lottery")
    private String eventCode;
}
