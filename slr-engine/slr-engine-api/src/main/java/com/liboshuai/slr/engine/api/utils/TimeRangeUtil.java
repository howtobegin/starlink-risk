package com.liboshuai.slr.engine.api.utils;

import com.liboshuai.slr.engine.api.dto.TimeRangeDTO;
import com.liboshuai.slr.engine.api.enums.TimeRangeEnum;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

public class TimeRangeUtil {


    /**
     * 判断给定的日期时间是否在指定的时间规则范围内。
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

    /**
     * 根据给定的时间戳和时间范围规则，获取下一个时间范围的结束时间戳
     *
     * @param timestamp 当前时间戳
     * @param rule      时间范围规则，包含时间范围的类型和其他相关参数
     * @return 下一个时间范围的结束时间戳，如果规则为空或不支持该时间范围类型，则返回null
     */
    public static Long getNextEndTimestamp(long timestamp, TimeRangeDTO rule) {
        if (rule == null) {
            return null;
        }

        LocalDateTime dateTime = LocalDateTimeUtils.convertTimestamp2LocalDateTime(timestamp);

        TimeRangeEnum type = TimeRangeEnum.fromCode(rule.getType());

        switch (type) {
            case DAILY:
                return getNextDailyEnd(dateTime, rule);
            case WEEKLY:
                return getNextWeeklyEnd(dateTime, rule);
            case MONTHLY:
                return getNextMonthlyEnd(dateTime, rule);
            case YEARLY_MONTH:
                return getNextYearlyMonthEnd(dateTime, rule);
            case YEARLY_DATE_RANGE:
                return getNextYearlyDateRangeEnd(dateTime, rule);
            default:
                return null;
        }
    }

    private static Long getNextDailyEnd(LocalDateTime dateTime, TimeRangeDTO rule) {
        LocalDateTime endDateTime = dateTime.with(rule.getEndTime());

        if (dateTime.isAfter(endDateTime)) {
            endDateTime = endDateTime.plusDays(1);
        }

        return LocalDateTimeUtils.convertLocalDateTime2Timestamp(endDateTime);
    }

    private static Long getNextWeeklyEnd(LocalDateTime dateTime, TimeRangeDTO rule) {
        List<DayOfWeek> daysOfWeek = rule.getDaysOfWeek().stream()
                .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                .collect(Collectors.toList());

        LocalDateTime endDateTime = dateTime.with(rule.getEndTime());

        if (daysOfWeek.contains(dateTime.getDayOfWeek()) && dateTime.isBefore(endDateTime)) {
            return LocalDateTimeUtils.convertLocalDateTime2Timestamp(endDateTime);
        }

        LocalDate nextDate = dateTime.toLocalDate();
        do {
            nextDate = nextDate.plusDays(1);
        } while (!daysOfWeek.contains(nextDate.getDayOfWeek()));
        LocalDateTime localDateTime = nextDate.atTime(rule.getEndTime());
        return LocalDateTimeUtils.convertLocalDateTime2Timestamp(localDateTime);
    }

    private static Long getNextMonthlyEnd(LocalDateTime dateTime, TimeRangeDTO rule) {
        int dayOfMonth = dateTime.getDayOfMonth();
        LocalDate nextEndDay = LocalDate.of(dateTime.getYear(), dateTime.getMonth(), rule.getEndDayOfMonth());

        if (dayOfMonth > rule.getEndDayOfMonth()) {
            nextEndDay = nextEndDay.plusMonths(1);
        }

        LocalDateTime localDateTime = nextEndDay.atTime(rule.getEndTime());
        return LocalDateTimeUtils.convertLocalDateTime2Timestamp(localDateTime);
    }

    private static Long getNextYearlyMonthEnd(LocalDateTime dateTime, TimeRangeDTO rule) {
        int currentMonth = dateTime.getMonthValue();
        int year = dateTime.getYear();

        LocalDate nextEndDate = LocalDate.of(year, rule.getEndMonth(), 1);
        if (currentMonth > rule.getEndMonth()) {
            nextEndDate = nextEndDate.plusYears(1);
        }

        LocalDateTime localDateTime = nextEndDate.atTime(rule.getEndTime());
        return LocalDateTimeUtils.convertLocalDateTime2Timestamp(localDateTime);
    }

    private static Long getNextYearlyDateRangeEnd(LocalDateTime dateTime, TimeRangeDTO rule) {
        MonthDay startMd = MonthDay.parse("--" + rule.getStartYearlyDate());
        MonthDay endMd = MonthDay.parse("--" + rule.getEndYearlyDate());
        MonthDay currentMd = MonthDay.from(dateTime.toLocalDate());

        Year currentYear = Year.of(dateTime.getYear());
        LocalDate endDate = endMd.atYear(currentYear.getValue());

        // 当当前时间 `dateTime` 超过 `endYearlyDate`，说明要到下一年的范围内处理
        if (currentMd.isAfter(endMd) || currentMd.isBefore(startMd)) {
            endDate = endMd.atYear(currentYear.getValue() + 1);
        }

        LocalDateTime localDateTime = endDate.atTime(rule.getEndTime());
        return LocalDateTimeUtils.convertLocalDateTime2Timestamp(localDateTime);
    }
}