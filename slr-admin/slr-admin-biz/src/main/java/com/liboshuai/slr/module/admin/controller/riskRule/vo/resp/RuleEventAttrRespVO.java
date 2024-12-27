package com.liboshuai.slr.module.admin.controller.riskRule.vo.resp;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
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

    /**
     * {@link RuleEventRespVO#getEventCode()}
     */
    @Schema(description = "事件编号", example = "GAME_LOTTERY")
    private String eventCode;

    @Schema(description = "属性编号", example = "A175928847299117063")
    private String attributeCode;

    @Schema(description = "属性名称", example = "订单号")
    private String attributeName;

    @Schema(description = "属性key", example = "orderNo")
    private String attributeKey;

    /**
     * {@link RuleEventAttrTypeEnum}
     */
    @Schema(description = "属性类型", example = "String")
    private String attributeType;

    @Schema(description = "属性值")
    private RuleEventAttrValueRespVO ruleEventAttrValueRespVO;
}
