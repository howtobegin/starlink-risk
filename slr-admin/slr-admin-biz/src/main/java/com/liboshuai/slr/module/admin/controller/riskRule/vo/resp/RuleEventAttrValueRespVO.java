package com.liboshuai.slr.module.admin.controller.riskRule.vo.resp;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
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
     * {@link RuleEventAttrRespVO#getAttrCode()}
     */
    @Schema(description = "属性编号", example = "GAME_userId_lottery_campaignId")
    private String attrCode;

    @Schema(description = "属性字段", example = "campaignId")
    private String attrFiled;

    @Schema(description = "属性名称", example = "活动id")
    private String attrName;

    /**
     * {@link RuleEventAttrTypeEnum}
     */
    @Schema(description = "属性类型", example = "String")
    private String attrType;

    /**
     * {@link RuleEventAttrOpEnum}
     */
    @Schema(description = "属性比较符", example = "==")
    private String attrOp;

    @Schema(description = "属性值", example = "C000000001")
    private String attrValue;

    /**
     * {@link RuleCondRespVO#getCondCode()}
     */
    @Schema(description = "条件编号", example = "R1553673459123456000_GAME_userId_lottery")
    private String condCode;
}
