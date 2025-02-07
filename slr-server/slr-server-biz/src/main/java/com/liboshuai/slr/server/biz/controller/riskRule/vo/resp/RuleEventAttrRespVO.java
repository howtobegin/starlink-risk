package com.liboshuai.slr.server.biz.controller.riskRule.vo.resp;

import com.liboshuai.slr.engine.api.enums.RuleEventAttrTypeEnum;
import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则事件属性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "属性编号", example = "GAME_userId_lottery_campaignId")
    private String attrCode;

    @Schema(description = "属性字段", example = "campaignId")
    private String attrField;

    @Schema(description = "属性名称", example = "活动id")
    private String attrName;

    /**
     * {@link RuleEventAttrTypeEnum}
     */
    @Schema(description = "属性类型", example = "String")
    private String attrType;

    /**
     * {@link RuleEventRespVO#getEventCode()}
     */
    @Schema(description = "事件编号", example = "GAME_LOTTERY")
    private String eventCode;

}
