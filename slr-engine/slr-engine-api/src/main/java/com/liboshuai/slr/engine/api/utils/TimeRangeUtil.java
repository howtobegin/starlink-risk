package com.liboshuai.slr.engine.api.utils;

import com.liboshuai.slr.engine.api.dto.TimeRangeDTO;

import java.time.*;
import java.util.List;

public class TimeRangeUtil {

    /**
     * 判断给定的 dateTime 是否满足某一条时间范围规则
     */
    public static boolean isWithinRule(LocalDateTime dateTime, TimeRangeDTO rule) {
        if (rule == null) {
            return false;
        }

        // 提取当日时间和日期信息
        LocalTime time = dateTime.toLocalTime();
        LocalDate date = dateTime.toLocalDate();

        switch (rule.getType()) {
            case DAILY:
                return isWithinDaily(time, rule);
            case WEEKLY:
                return isWithinWeekly(dateTime, rule);
            case MONTHLY:
                return isWithinMonthly(date, time, rule);
            case YEARLY_MONTH:
                return isWithinYearlyMonth(date, time, rule);
            case YEARLY_DATE_RANGE:
                return isWithinYearlyDateRange(date, time, rule);
            default:
                return false;
        }
    }

    // ============= DAILY =============
    private static boolean isWithinDaily(LocalTime time, TimeRangeDTO rule) {
        LocalTime start = rule.getStartTime();
        LocalTime end = rule.getEndTime();
        if (start == null || end == null) {
            return false;
        }
        // 简单处理：start <= end
        // 若要支持跨夜(例如 22:00 - 02:00)，需额外处理
        return !time.isBefore(start) && !time.isAfter(end);
    }

    // ============= WEEKLY =============
    private static boolean isWithinWeekly(LocalDateTime dateTime, TimeRangeDTO rule) {
        LocalTime time = dateTime.toLocalTime();
        LocalTime start = rule.getStartTime();
        LocalTime end = rule.getEndTime();
        List<DayOfWeek> days = rule.getDaysOfWeek();
        if (days == null || days.isEmpty() || start == null || end == null) {
            return false;
        }
        // 判断星期几匹配
        if (!days.contains(dateTime.getDayOfWeek())) {
            return false;
        }
        // 判断时间范围
        return !time.isBefore(start) && !time.isAfter(end);
    }

    // ============= MONTHLY =============
    private static boolean isWithinMonthly(LocalDate date, LocalTime time, TimeRangeDTO rule) {
        Integer startDay = rule.getStartDayOfMonth();
        Integer endDay = rule.getEndDayOfMonth();
        LocalTime startTime = rule.getStartTime();
        LocalTime endTime = rule.getEndTime();

        if (startDay == null || endDay == null || startTime == null || endTime == null) {
            return false;
        }
        int dayOfMonth = date.getDayOfMonth();
        // 判断日期是否在 [startDay, endDay] 之间
        if (dayOfMonth < startDay || dayOfMonth > endDay) {
            return false;
        }
        // 判断时间是否在 [startTime, endTime] 之间
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    // ============= YEARLY_MONTH =============
    private static boolean isWithinYearlyMonth(LocalDate date, LocalTime time, TimeRangeDTO rule) {
        Integer startMonth = rule.getStartMonth();
        Integer endMonth = rule.getEndMonth();
        LocalTime startTime = rule.getStartTime();
        LocalTime endTime = rule.getEndTime();

        if (startMonth == null || endMonth == null || startTime == null || endTime == null) {
            return false;
        }
        int monthValue = date.getMonthValue();

        if (startMonth <= endMonth) {
            // 普通情况：3 月 - 6 月
            if (monthValue < startMonth || monthValue > endMonth) {
                return false;
            }
        } else {
            // 跨年情况：例如 11 月 - 2 月
            // monthValue 在 [startMonth, 12] 或 [1, endMonth]
            if (monthValue < startMonth && monthValue > endMonth) {
                return false;
            }
        }
        // 判断时间是否在 [startTime, endTime] 之间
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    // ============= YEARLY_DATE_RANGE =============
    private static boolean isWithinYearlyDateRange(LocalDate date, LocalTime time, TimeRangeDTO rule) {
        MonthDay startMd = rule.getStartYearlyDate();
        MonthDay endMd = rule.getEndYearlyDate();
        LocalTime startTime = rule.getStartTime();
        LocalTime endTime = rule.getEndTime();

        if (startMd == null || endMd == null || startTime == null || endTime == null) {
            return false;
        }

        MonthDay currentMd = MonthDay.from(date);
        boolean dateInRange;
        if (startMd.equals(endMd)) {
            // 起止日期相同，视为当日
            dateInRange = currentMd.equals(startMd);
        } else if (startMd.isBefore(endMd)) {
            // 正常顺序，例如：3月1日-6月1日
            dateInRange = !currentMd.isBefore(startMd) && !currentMd.isAfter(endMd);
        } else {
            // 跨年情况，例如：12月25日 - 次年1月5日
            // currentMd >= startMd (12月25日-12月31日) 或 currentMd <= endMd (1月1日-1月5日)
            dateInRange = !currentMd.isBefore(startMd) || !currentMd.isAfter(endMd);
        }

        if (!dateInRange) {
            return false;
        }
        // 判断时间是否在 [startTime, endTime] 之间
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
}
