package com.liboshuai.slr.module.admin.pojo.vo.riskRule;

import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.RuleAuditOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 事件属性组
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleEventAttrRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;


    @Schema(description = "事件编号", example = "GAME_LOTTERY")
    private String eventCode;

    @Schema(description = "属性名称", example = "订单号")
    private String attributeName;

    @Schema(description = "属性key", example = "orderNo")
    private String attributeKey;

    /**
     * {@link RuleEventAttrTypeEnum}
     */
    @Schema(description = "属性类型", example = "String")
    private String attributeType;

    @Schema(description = "属性值", example = "C175928847299117065")
    private String attributeValue;

    /**
     * {@link RuleAuditOpEnum}
     */
    @Schema(description = "属性比较符", example = "C175928847299117065")
    private String attributeOp;
}
