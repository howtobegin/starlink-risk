package com.liboshuai.slr.module.admin.controller.riskRule.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import com.liboshuai.slr.module.engine.enums.RuleCondTypeEnum;
import com.liboshuai.slr.module.engine.enums.TimeUnitEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则条件组
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleCondRespVO extends BaseRespVO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "条件编号", example = "R1553673459123456000_GAME_userId_lottery")
    private String condCode;

    /**
     * {@link RuleCondTypeEnum}
     */
    @Schema(description = "条件类型", example = "CYCLE")
    private String condType;

    @Schema(description = "窗口值,仅在条件类型为周期时使用", example = "10")
    private Long windowValue;

    /**
     * {@link TimeUnitEnum}
     */
    @Schema(description = "窗口单位,仅在条件类型为周期时使用", example = "分钟")
    private String windowUnit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "开始时间,仅在条件类型为范围时使用", example = "2025-01-01 00:00:00")
    private LocalDateTime beginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "结束时间,仅在条件类型为范围时使用", example = "2025-02-01 00:00:00")
    private LocalDateTime endTime;

    @Schema(description = "阈值", example = "20")
    private Long threshold;

    @Schema(description = "是否跨历史数据", example = "true")
    private Boolean crossHistory;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "跨历史时间点", example = "2025-03-01 10:00:01")
    private LocalDateTime crossHistoryTimeline;

    /**
     * {@link RuleInfoRespVO#getRuleCode()}
     */
    @Schema(description = "规则编号", example = "R1553673459123456000")
    private String ruleCode;

    /**
     * {@link RuleEventRespVO#getEventCode()}
     */
    @Schema(description = "事件编号", example = "GAME_userId_lottery")
    private String eventCode;

    @Schema(description = "事件名称", example = "游戏抽奖")
    private String eventName;

    @Schema(description = "事件属性值组")
    private List<RuleEventAttrValueRespVO> ruleEventAttrValueGroup;

}
