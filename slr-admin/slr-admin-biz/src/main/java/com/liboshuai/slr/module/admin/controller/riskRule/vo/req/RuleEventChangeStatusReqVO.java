package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.liboshuai.slr.framework.common.enums.CommonAuditOpEnum;
import com.liboshuai.slr.framework.common.enums.CommonStatusEnum;
import com.liboshuai.slr.framework.common.validation.InStringEnum;
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
@Schema(description = "管理后台 - 变更状态风控事件信息 Request VO")
public class RuleEventChangeStatusReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "事件编号[eventCode]，不能为空")
    @Schema(description = "事件编号", example = "GAME_userId_lottery", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventCode;

    /**
     * {@link CommonStatusEnum}
     */
    @NotBlank(message = "新事件状态[newEventStatus]，不能为空")
    @InStringEnum(value = CommonStatusEnum.class, message = "新事件状态[newEventStatus]，必须在指定范围 {value}")
    @Schema(description = "新事件状态", example = "ONLINE_PENDING", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newEventStatus;

    /**
     * 审核操作
     * {@link CommonAuditOpEnum}
     */
    @InStringEnum(value = CommonAuditOpEnum.class, message = "审核操作[auditOp]，必须在指定范围 {value}")
    @Schema(description = "审核操作", example = "APPROVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String auditOp;
}
