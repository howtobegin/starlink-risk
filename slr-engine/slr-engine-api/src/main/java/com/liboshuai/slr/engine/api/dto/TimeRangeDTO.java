package com.liboshuai.slr.engine.api.dto;

import com.liboshuai.slr.engine.api.type.TimeRangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(TimeRangeType.class)
public class TimeRangeDTO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 规则类型
     * {@link com.liboshuai.slr.engine.api.enums.TimeRangeEnum}
     */
    private String type;

    // ============ 通用的时间段（时-分-秒） ============
    private LocalTime startTime; // 例如 08:00
    private LocalTime endTime;   // 例如 18:00

    // ============ WEEKLY 时使用 ============
    /**
     * 每周哪些天，例：周一、周三、周五
     * {@link com.liboshuai.slr.engine.api.enums.WeekEnum}
     */
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