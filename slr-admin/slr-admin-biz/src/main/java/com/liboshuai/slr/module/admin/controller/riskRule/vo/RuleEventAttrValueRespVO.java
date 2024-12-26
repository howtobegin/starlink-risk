package com.liboshuai.slr.module.admin.controller.riskRule.vo;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.RuleAuditOpEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 风控规则事件属性值
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrValueRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    /**
     * {@link RuleCondRespVO#getCondCode()}
     */
    @Schema(description = "条件编号", example = "C175928847299117063")
    private String condCode;

    /**
     * {@link RuleEventAttrRespVO#getAttributeCode()}
     */
    @Schema(description = "属性编号", example = "A175928847299117063")
    private String attributeCode;

    @Schema(description = "属性值", example = "C175928847299117065")
    private String attributeValue;

    /**
     * {@link RuleAuditOpEnum}
     */
    @Schema(description = "属性比较符", example = "C175928847299117065")
    private String attributeOp;
}
