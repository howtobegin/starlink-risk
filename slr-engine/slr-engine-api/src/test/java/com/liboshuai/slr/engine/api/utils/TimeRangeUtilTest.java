package com.liboshuai.slr.engine.api.utils;

import com.liboshuai.slr.engine.api.dto.TimeRangeDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class TimeRangeUtilTest {

    // ========== DAILY（每日时间范围）测试 ==========
    @Test
    void testDailyWithinTimeRange() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("daily")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        LocalDateTime testTime1 = LocalDateTime.of(2023, 6, 1, 9, 0); // 在范围内
        LocalDateTime testTime2 = LocalDateTime.of(2023, 6, 1, 20, 0); // 超出范围

        assertThat(TimeRangeUtil.isWithinRule(testTime1, rule)).isTrue();
        assertThat(TimeRangeUtil.isWithinRule(testTime2, rule)).isFalse();
    }

    @Test
    void testDailyWithinTimeRange2() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("daily")
                .startTime(LocalTime.of(0, 0, 0))
                .endTime(LocalTime.of(23, 59, 59, 999999999))
                .build();

        LocalDateTime testTime1 = LocalDateTime.of(2023, 6, 1, 9, 0); // 在范围内
        LocalDateTime testTime2 = LocalDateTime.of(2023, 6, 2, 23, 0); // 在范围内

        assertThat(TimeRangeUtil.isWithinRule(testTime1, rule)).isTrue();
        assertThat(TimeRangeUtil.isWithinRule(testTime2, rule)).isTrue();
    }

    // ========== WEEKLY（每周特定天的时间范围）测试 ==========
    @Test
    void testWeeklyWithinTimeRange() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("weekly")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .daysOfWeek(Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY")) // 允许周一、周三、周五
                .build();

        LocalDateTime mondayTime = LocalDateTime.of(2023, 6, 12, 10, 0); // 周一，范围内
        LocalDateTime sundayTime = LocalDateTime.of(2023, 6, 11, 10, 0); // 周日，不在允许范围

        assertThat(TimeRangeUtil.isWithinRule(mondayTime, rule)).isTrue();
        assertThat(TimeRangeUtil.isWithinRule(sundayTime, rule)).isFalse();
    }

    // ========== MONTHLY（每月特定日期的时间范围）测试 ==========
    @Test
    void testMonthlyWithinTimeRange() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("monthly")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .startDayOfMonth(5)
                .endDayOfMonth(20)
                .build();

        LocalDateTime testTime1 = LocalDateTime.of(2023, 6, 10, 10, 0); // 6月10日，在范围内
        LocalDateTime testTime2 = LocalDateTime.of(2023, 6, 25, 14, 0); // 6月25日，超出范围

        assertThat(TimeRangeUtil.isWithinRule(testTime1, rule)).isTrue();
        assertThat(TimeRangeUtil.isWithinRule(testTime2, rule)).isFalse();
    }

    // ========== YEARLY_MONTH（每年特定月份的时间范围）测试 ==========
    @Test
    void testYearlyMonthWithinTimeRange() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("yearly_month")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(15, 0))
                .startMonth(3)
                .endMonth(6)
                .build();

        LocalDateTime testTime1 = LocalDateTime.of(2023, 4, 10, 11, 0); // 4月，在范围内
        LocalDateTime testTime2 = LocalDateTime.of(2023, 12, 10, 11, 0); // 12月，超出范围

        assertThat(TimeRangeUtil.isWithinRule(testTime1, rule)).isTrue();
        assertThat(TimeRangeUtil.isWithinRule(testTime2, rule)).isFalse();
    }

    // ========== YEARLY_DATE_RANGE（每年特定日期区间，支持跨年）测试 ==========
    @Test
    void testYearlyDateRangeWithinTimeRange() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("yearly_date_range")
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(22, 0))
                .startYearlyDate("12-25")
                .endYearlyDate("01-05")
                .build();

        LocalDateTime testTime1 = LocalDateTime.of(2023, 12, 27, 8, 0); // 在 12月份范围内
        LocalDateTime testTime2 = LocalDateTime.of(2024, 1, 3, 8, 0); // 在 1月份范围内
        LocalDateTime testTime3 = LocalDateTime.of(2023, 2, 5, 8, 0); // 2月，超出范围

        assertThat(TimeRangeUtil.isWithinRule(testTime1, rule)).isTrue();
        assertThat(TimeRangeUtil.isWithinRule(testTime2, rule)).isTrue();
        assertThat(TimeRangeUtil.isWithinRule(testTime3, rule)).isFalse();
    }

    // ========== 跨夜时间段测试（如 22:00 ~ 03:00） ==========
    @Test
    void testCrossNightTimeRange() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("daily")
                .startTime(LocalTime.of(22, 0))
                .endTime(LocalTime.of(3, 0))
                .build();

        LocalDateTime testTime1 = LocalDateTime.of(2023, 6, 1, 23, 0); // 23:00，属于跨夜范围
        LocalDateTime testTime2 = LocalDateTime.of(2023, 6, 2, 2, 30); // 02:30，也属于跨夜范围
        LocalDateTime testTime3 = LocalDateTime.of(2023, 6, 2, 11, 30); // 11:30，不属于范围

        assertThat(TimeRangeUtil.isWithinRule(testTime1, rule)).isTrue();
        assertThat(TimeRangeUtil.isWithinRule(testTime2, rule)).isTrue();
        assertThat(TimeRangeUtil.isWithinRule(testTime3, rule)).isFalse();
    }
}