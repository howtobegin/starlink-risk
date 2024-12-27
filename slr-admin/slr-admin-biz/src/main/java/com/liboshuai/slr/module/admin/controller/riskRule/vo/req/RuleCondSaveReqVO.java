package com.liboshuai.slr.module.admin.controller.riskRule.vo.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liboshuai.slr.framework.common.validation.InStringEnum;
import com.liboshuai.slr.module.engine.enums.RuleCondCombOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleCondTypeEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    @Schema(description = "规则编号（无需前端传入）", example = "A175928847299117063")
    private String ruleCode;

    @NotBlank(message = "事件编号[eventCode]，不能为空")
    @Schema(description = "事件编号", example = "GAME_LOTTERY")
    private String eventCode;

    @Schema(description = "条件编号（无需前端传入）", example = "C175928847299117063")
    private String condCode;

    /**
     * {@link RuleCondTypeEnum}
     */
    @NotBlank(message = "条件类型[condType]，不能为空")
    @InStringEnum(value = RuleCondCombOpEnum.class, message = "条件类型[condType]，必须在指定范围 {value}")
    @Schema(description = "条件类型", example = "RANGE")
    private String condType;

    @Schema(description = "窗口值,仅在条件类型为周期时使用", example = "10")
    private Long windowValue;

    /**
     * {@link TimeUnitEnum}
     */
    @InStringEnum(value = TimeUnitEnum.class, message = "窗口单位[windowUnit]，必须在指定范围 {value}")
    @Schema(description = "窗口单位,仅在条件类型为周期时使用", example = "MINUTE")
    private String windowUnit;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "开始时间,仅在条件类型为范围时使用", example = "2025-01-01 00:00:00")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "结束时间,仅在条件类型为范围时使用", example = "2025-02-01 00:00:00")
    private LocalDateTime endTime;

    @NotNull(message = "阈值[threshold]，不能为空")
    @Schema(description = "阈值", example = "20")
    private Long threshold;

    @NotNull(message = "是否跨历史数据[crossHistory]，不能为空")
    @Schema(description = "是否跨历史数据", example = "true")
    private Boolean crossHistory;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "跨历史时间点", example = "2025-03-01 10:00:01")
    private LocalDateTime crossHistoryTimeline;

    @NotNull(message = "事件属性组[ruleEventAttrSaveReqVOList]，不能为空")
    @Schema(description = "事件属性组")
    private List<RuleEventAttrValueSaveReqVO> ruleEventAttrValueSaveReqVOList;

}
