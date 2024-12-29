package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.Size;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrSaveReqVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    @Size(max = 256, message = "属性编号[attrCode]，长度不能超过 256 个字符")
    @Schema(description = "属性编号", example = "GAME_userId_lottery_campaignId")
    private String attrCode;

    @Size(max = 32, message = "属性名称[attrCode]，长度不能超过 32 个字符")
    @Schema(description = "属性名称", example = "订单号")
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
