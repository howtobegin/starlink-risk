package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrSaveReqVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "属性编号", example = "GAME_userId_lottery_campaignId")
    private String attrCode;

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
    @Schema(description = "事件编号", example = "GAME_userId_lottery")
    private String eventCode;
}
