package com.liboshuai.slr.engine.api.utils;

import com.liboshuai.slr.engine.api.dto.TimeRangeDTO;
import com.liboshuai.slr.engine.api.enums.TimeRangeEnum;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

public class TimeRangeUtil {

    /**
     * 判断给定的 `dateTime` 是否满足某一条时间范围规则
     */
    public static boolean isWithinRule(LocalDateTime dateTime, TimeRangeDTO rule) {
        if (rule == null) {
            return false;
        }

        TimeRangeEnum type;
        try {
            type = TimeRangeEnum.fromCode(rule.getType());
        } catch (IllegalArgumentException e) {
            return false;
        }

        LocalTime time = dateTime.toLocalTime();
        LocalDate date = dateTime.toLocalDate();

        switch (type) {
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
        return isWithinTimeRange(time, rule.getStartTime(), rule.getEndTime());
    }

    // ============= WEEKLY =============
    private static boolean isWithinWeekly(LocalDateTime dateTime, TimeRangeDTO rule) {
        LocalTime time = dateTime.toLocalTime();

        // 将 List<String> 转换为 List<DayOfWeek>
        List<DayOfWeek> daysOfWeek = rule.getDaysOfWeek().stream()
                .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                .collect(Collectors.toList());

        if (daysOfWeek.isEmpty()) {
            return false;
        }

        if (!daysOfWeek.contains(dateTime.getDayOfWeek())) {
            return false;
        }

        return isWithinTimeRange(time, rule.getStartTime(), rule.getEndTime());
    }

    // ============= MONTHLY =============
    private static boolean isWithinMonthly(LocalDate date, LocalTime time, TimeRangeDTO rule) {
        Integer startDay = rule.getStartDayOfMonth();
        Integer endDay = rule.getEndDayOfMonth();

        if (startDay == null || endDay == null) {
            return false;
        }

        int dayOfMonth = date.getDayOfMonth();
        if (dayOfMonth < startDay || dayOfMonth > endDay) {
            return false;
        }

        return isWithinTimeRange(time, rule.getStartTime(), rule.getEndTime());
    }

    // ============= YEARLY_MONTH =============
    private static boolean isWithinYearlyMonth(LocalDate date, LocalTime time, TimeRangeDTO rule) {
        Integer startMonth = rule.getStartMonth();
        Integer endMonth = rule.getEndMonth();

        if (startMonth == null || endMonth == null) {
            return false;
        }

        int monthValue = date.getMonthValue();
        boolean inMonthRange = startMonth <= endMonth
                ? (monthValue >= startMonth && monthValue <= endMonth)
                : (monthValue >= startMonth || monthValue <= endMonth);

        if (!inMonthRange) {
            return false;
        }

        return isWithinTimeRange(time, rule.getStartTime(), rule.getEndTime());
    }

    // ============= YEARLY_DATE_RANGE =============
    private static boolean isWithinYearlyDateRange(LocalDate date, LocalTime time, TimeRangeDTO rule) {
        if (rule.getStartYearlyDate() == null || rule.getEndYearlyDate() == null) {
            return false;
        }

        MonthDay startMd = MonthDay.parse("--" + rule.getStartYearlyDate());
        MonthDay endMd = MonthDay.parse("--" + rule.getEndYearlyDate());

        MonthDay currentMd = MonthDay.from(date);
        boolean dateInRange = startMd.isBefore(endMd)
                ? (!currentMd.isBefore(startMd) && !currentMd.isAfter(endMd))
                : (!currentMd.isBefore(startMd) || !currentMd.isAfter(endMd));

        if (!dateInRange) {
            return false;
        }

        return isWithinTimeRange(time, rule.getStartTime(), rule.getEndTime());
    }

    /**
     * 判断时间是否落入 start 至 end 之间（支持跨夜时间段）
     */
    private static boolean isWithinTimeRange(LocalTime time, LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return false;
        }
        if (start.isBefore(end)) {
            return !time.isBefore(start) && !time.isAfter(end);
        } else {
            return !time.isBefore(start) || !time.isAfter(end);
        }
    }
}