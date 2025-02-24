package com.liboshuai.slr.server.biz.controller.rule.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liboshuai.slr.framework.common.pojo.BaseRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class TimeRangeRespVO extends BaseRespVO {

    private static final long serialVersionUID = 1L;


    /**
     * {@link com.liboshuai.slr.engine.api.enums.TimeRangeEnum}
     */
    @Schema(description = "类型", example = "daily")
    private String type;

    // ============ 通用的时间段（时-分-秒） ============
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "开始时间", example = "08:00")
    private LocalTime startTime;
    @Schema(description = "结束时间", example = "18:00")
    private LocalTime endTime;

    // ============ WEEKLY 时使用 ============
    // 每周哪些天，例：周一、周三、周五
    private List<String> daysOfWeek;

    // ============ MONTHLY 时使用 ============
    // 每月的第几号到第几号
    private Integer startDayOfMonth; // 例如 5
    private Integer endDayOfMonth;   // 例如 10

    // ============ YEARLY_MONTH 时使用 ============
    // 每年的哪几个月份区间，例：3 - 6 表示 3月到6月
    private Integer startMonth;      // 例如 3
    private Integer endMonth;        // 例如 6

    // ============ YEARLY_DATE_RANGE 时使用 ============
    // 每年的固定日期区间，可能跨年
    // 例如：12月25日 到 次年1月5日
    // 这里用 MonthDay 表示只关注“月-日”而忽略具体年份
    private String startYearlyDate; // 例如 12-25
    private String endYearlyDate;   // 例如 01-05

    // 条件编号
    private String condCode;
}
