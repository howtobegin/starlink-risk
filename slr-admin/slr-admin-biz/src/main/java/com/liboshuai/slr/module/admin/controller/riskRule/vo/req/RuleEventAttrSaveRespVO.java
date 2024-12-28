package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.validation.InStringEnum;
import com.liboshuai.slr.module.admin.controller.riskRule.vo.resp.RuleEventRespVO;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleEventAttrSaveRespVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    /**
     * {@link RuleEventRespVO#getEventCode()}
     */
    @NotBlank(message = "事件编号[eventCode]，不能为空")
    @Size(max = 64, message = "事件编号[eventCode]，长度不能超过 64 个字符")
    @Schema(description = "事件编号", example = "GAME_LOTTERY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventCode;

    @Schema(description = "属性编号", example = "A175928847299117063")
    private String attributeCode;

    @NotBlank(message = "属性名称[attributeName]，不能为空")
    @Size(max = 64, message = "属性名称[attributeName]，长度不能超过 64 个字符")
    @Schema(description = "属性名称", example = "订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attributeName;

    @NotBlank(message = "属性key[attributeKey]，不能为空")
    @Size(max = 64, message = "属性key[attributeKey]，长度不能超过 64 个字符")
    @Schema(description = "属性key", example = "orderNo", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attributeKey;

    /**
     * {@link RuleEventAttrTypeEnum}
     */
    @NotBlank(message = "属性类型[attributeType]，不能为空")
    @InStringEnum(value = RuleEventAttrTypeEnum.class, message = "属性类型[attributeType]，必须在指定范围 {value}")
    @Schema(description = "属性类型", example = "String", requiredMode = Schema.RequiredMode.REQUIRED)
    private String attributeType;
}
