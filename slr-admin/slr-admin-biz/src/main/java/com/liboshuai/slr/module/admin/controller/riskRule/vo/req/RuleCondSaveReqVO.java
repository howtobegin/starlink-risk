package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liboshuai.slr.framework.common.validation.InStringEnum;
import com.liboshuai.slr.module.engine.enums.RuleCondTypeEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RuleCondSaveReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = 256, message = "条件编号[condCode]，长度不能超过 256 个字符")
    @Schema(description = "条件编号", example = "R1553673459123456000_GAME_userId_lottery", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String condCode;

    /**
     * {@link RuleCondTypeEnum}
     */
    @NotBlank(message = "条件类型[condType]，不能为空")
    @InStringEnum(value = RuleCondTypeEnum.class, message = "条件类型[condType]，必须在指定范围 {value}")
    @Schema(description = "条件类型", example = "RANGE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String condType;

    @Schema(description = "窗口值,仅在条件类型为周期时使用", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long windowValue;

    /**
     * {@link TimeUnitEnum}
     */
    @InStringEnum(value = TimeUnitEnum.class, message = "窗口单位[windowUnit]，必须在指定范围 {value}")
    @Schema(description = "窗口单位,仅在条件类型为周期时使用", example = "分钟", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String windowUnit;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "开始时间,仅在条件类型为范围时使用", example = "2025-01-01 00:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "结束时间,仅在条件类型为范围时使用", example = "2025-02-01 00:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime endTime;

    @NotNull(message = "阈值[threshold]，不能为空")
    @Schema(description = "阈值", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long threshold;

    @NotNull(message = "是否跨历史数据[crossHistory]，不能为空")
    @Schema(description = "是否跨历史数据", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean crossHistory;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "跨历史时间点", example = "2025-03-01 10:00:01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime crossHistoryTimeline;

    @Size(max = 256, message = "规则编号[ruleCode]，长度不能超过 256 个字符")
    @Schema(description = "规则编号", example = "1553673459123456000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long ruleCode;

    @Size(max = 256, message = "事件编号[eventCode]，长度不能超过 256 个字符")
    @NotBlank(message = "事件编号[eventCode]，不能为空")
    @Schema(description = "事件编号", example = "GAME_userId_lottery", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventCode;

    @Valid
    @Schema(description = "事件属性值组", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<RuleEventAttrValueSaveReqVO> ruleEventAttrValueGroup;

}
