package com.liboshuai.slr.server.biz.controller.rule.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liboshuai.slr.engine.api.enums.RuleCondTypeEnum;
import com.liboshuai.slr.engine.api.enums.TimeUnitEnum;
import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

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

    @Schema(description = "条件编号", example = "R1553673459123456000_game_userId_lottery")
    private String condCode;

    /**
     * {@link RuleCondTypeEnum}
     */
    @Schema(description = "条件类型", example = "recent")
    private String condType;

    @Schema(description = "窗口值,仅在条件类型为最近时间时使用", example = "10")
    private Long windowValue;

    /**
     * {@link TimeUnitEnum}
     */
    @Schema(description = "窗口单位,仅在条件类型为最近时间时使用", example = "分钟")
    private String windowUnit;

    @Schema(description = "时间范围，仅在条件类型为范围时间时使用")
    private TimeRangeRespVO timeRange;

    @Schema(description = "阈值", example = "20")
    private Long threshold;

    @Schema(description = "阈值缩放因子", example = "2")
    private Long thresholdScaleFactor;

    @Schema(description = "是否跨历史数据", example = "true")
    private Boolean crossHistory;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "跨历史时间点", example = "2025-03-01 10:00:01")
    private LocalDateTime crossHistoryTimeline;

    /**
     * {@link RuleInfoRespVO#getRuleCode()}
     */
    @Schema(description = "规则编号", example = "1553673459123456000")
    private Long ruleCode;

    /**
     * {@link RuleEventRespVO#getEventCode()}
     */
    @Schema(description = "事件编号", example = "game_userId_lottery")
    private String eventCode;

    @Schema(description = "事件字段", example = "lottery")
    private String eventField;

    @Schema(description = "事件名称", example = "游戏抽奖")
    private String eventName;

    @Schema(description = "事件属性值组")
    private List<RuleEventAttrValueRespVO> ruleEventAttrValueGroup;

}
