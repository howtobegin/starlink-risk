package com.liboshuai.slr.server.biz.controller.rule.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TimeRangeSaveReqVO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * {@link com.liboshuai.slr.engine.api.enums.TimeRangeEnum}
     */
    @Schema(description = "类型", example = "daily")
    private String type;

    // ============ 通用的时间段（时-分） ============
    @Schema(description = "开始时间（时-分-秒）", example = "08:00:00")
    private String startTime;

    @Schema(description = "结束时间（时-分-秒）", example = "18:00:00")
    private String endTime;

    // ============ WEEKLY 时使用 ============
    @Schema(description = "每周哪些天", example = "MONDAY,WEDNESDAY,FRIDAY")
    private List<String> daysOfWeek;

    // ============ MONTHLY 时使用 ============
    @Schema(description = "每月的几号到几号-开始日", example = "5")
    private Integer startDayOfMonth;
    @Schema(description = "每月的几号到几号-结束日", example = "10")
    private Integer endDayOfMonth;

    // ============ YEARLY_MONTH 时使用 ============
    @Schema(description = "每年的哪几个月份区间-开始月", example = "3")
    private Integer startMonth;
    @Schema(description = "每年的哪几个月份区间-结束月", example = "6")
    private Integer endMonth;

    // ============ YEARLY_DATE_RANGE 时使用 ============
    @Schema(description = "每年的固定日期区间-开始月日", example = "12-25")
    private String startYearlyDate;
    @Schema(description = "每年的固定日期区间-结束月日", example = "01-05")
    private String endYearlyDate;

    // 条件编号
    @Schema(description = "条件编号", example = "1890222324268011520_game_userId_lottery")
    private String condCode;
}
