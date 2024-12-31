package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.common.validation.InStringEnum;
import com.liboshuai.slr.module.engine.enums.RuleAuditOpEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "管理后台 - 变更状态风控规则信息 Request VO")
public class RuleInfoChangeStatusReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "规则编号[ruleCode]，不能为空")
    @Schema(description = "规则编号", example = "R175928847299117063", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ruleCode;

    /**
     * {@link CommonStatusEnum}
     */
    @NotBlank(message = "新规则状态[newRuleStatus]，不能为空")
    @InStringEnum(value = CommonStatusEnum.class, message = "新规则状态[newRuleStatus]，必须在指定范围 {value}")
    @Schema(description = "新规则状态", example = "ONLINE_PENDING", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newRuleStatus;

    /**
     * 审核操作
     * {@link RuleAuditOpEnum}
     */
    @InStringEnum(value = RuleAuditOpEnum.class, message = "审核操作[auditOp]，必须在指定范围 {value}")
    @Schema(description = "审核操作", example = "APPROVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String auditOp;
}
