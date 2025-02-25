package com.liboshuai.slr.engine.api.utils;

import com.liboshuai.slr.engine.api.dto.TimeRangeDTO;
import com.liboshuai.slr.framework.common.util.date.LocalDateTimeUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TimeRangeUtilTest {

    // ============= DAILY 规则 =============
    @Test
    void testIsWithinDaily() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("daily")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 10, 10, 0), rule)); // 在 9:00 - 18:00 内
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 10, 8, 59), rule)); // 早于 9:00
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 10, 18, 1), rule)); // 晚于 18:00
    }

    // ============= WEEKLY 规则 =============
    @Test
    void testIsWithinWeekly() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("weekly")
                .daysOfWeek(Arrays.asList("monday", "wednesday", "friday"))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        // 周一，时间符合
        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 10, 10, 0), rule)); // 6月10日是周一

        // 周二，不符合
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 11, 10, 0), rule)); // 6月11日是周二

        // 周五，时间符合
        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 14, 16, 0), rule)); // 6月14日是周五

        // 周三，时间不符合（早于开始时间）
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 12, 8, 0), rule)); // 6月12日是周三
    }

    // ============= MONTHLY 规则 =============
    @Test
    void testIsWithinMonthly() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("monthly")
                .startDayOfMonth(5)
                .endDayOfMonth(15)
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(23, 0))
                .build();

        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 10, 12, 0), rule)); // 6月10日，符合
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 4, 12, 0), rule)); // 6月4日，不符合
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 16, 12, 0), rule)); // 6月16日，不符合
    }

    // ============= YEARLY_MONTH 规则 =============
    @Test
    void testIsWithinYearlyMonth() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("yearly_month")
                .startMonth(3)
                .endMonth(6)
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(22, 0))
                .build();

        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 3, 15, 12, 0), rule)); // 3月，符合
        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 5, 12, 0), rule)); // 6月，符合
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 7, 5, 12, 0), rule)); // 7月，不符合
    }

    // ============= YEARLY_DATE_RANGE 规则 =============
    @Test
    void testIsWithinYearlyDateRange() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("yearly_date_range")
                .startYearlyDate("12-15")
                .endYearlyDate("01-10")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(20, 0))
                .build();

        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 12, 25, 12, 0), rule)); // 12月25日，符合
        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2025, 1, 8, 12, 0), rule)); // 次年1月8日，符合
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 2, 8, 12, 0), rule)); // 2月，超出范围
    }

    // ============= 跨夜时间段 规则 =============
    @Test
    void testIsWithinCrossNightTimeRange() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("daily")
                .startTime(LocalTime.of(22, 0))
                .endTime(LocalTime.of(3, 0))
                .build();

        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 10, 23, 0), rule)); // 23:00 符合
        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 11, 2, 30), rule)); // 02:30 符合
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 10, 21, 59), rule)); // 21:59 不符合
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.of(2024, 6, 11, 3, 01), rule)); // 03:01 不符合
    }

    // ============= 无效输入情况  =============
    @Test
    void testInvalidInputs() {
        TimeRangeDTO rule = TimeRangeDTO.builder().build();

        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.now(), null)); // null 规则
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.now(), rule)); // 规则类型为空
        rule.setType("invalid_type");
        assertFalse(TimeRangeUtil.isWithinRule(LocalDateTime.now(), rule)); // 无效的规则类型
    }

    /**
     * 测试 DAILY 类型
     */
    @Test
    void testGetNextDailyEnd_CurrentDay() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("daily")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        LocalDateTime now = LocalDateTime.of(2021, 1, 1, 9, 0);
        LocalDateTime expected = LocalDateTime.of(2021, 1, 1, 18, 0);
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }

    @Test
    void testGetNextDailyEnd_NextDay() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("daily")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        LocalDateTime now = LocalDateTime.of(2021, 1, 1, 19, 0);
        LocalDateTime expected = LocalDateTime.of(2021, 1, 2, 18, 0);
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }

    /**
     * 测试 WEEKLY 类型
     */
    @Test
    void testGetNextWeeklyEnd_CurrentDay() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("weekly")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .daysOfWeek(Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY"))
                .build();

        LocalDateTime now = LocalDateTime.of(2021, 1, 1, 9, 0); // Friday
        LocalDateTime expected = LocalDateTime.of(2021, 1, 1, 18, 0); // Still Friday
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }

    @Test
    void testGetNextWeeklyEnd_NextOccurrence() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("weekly")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .daysOfWeek(Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY"))
                .build();

        LocalDateTime now = LocalDateTime.of(2021, 1, 1, 19, 0); // Friday, after time
        LocalDateTime expected = LocalDateTime.of(2021, 1, 4, 18, 0); // Next Monday 18:00
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }

    /**
     * 测试 MONTHLY 类型
     */
    @Test
    void testGetNextMonthlyEnd_CurrentMonth() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("monthly")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .startDayOfMonth(3)
                .endDayOfMonth(6)
                .build();

        LocalDateTime now = LocalDateTime.of(2021, 1, 1, 9, 0);
        LocalDateTime expected = LocalDateTime.of(2021, 1, 6, 18, 0);
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }

    @Test
    void testGetNextMonthlyEnd_NextMonth() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("monthly")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .startDayOfMonth(3)
                .endDayOfMonth(6)
                .build();

        LocalDateTime now = LocalDateTime.of(2021, 1, 7, 9, 0);
        LocalDateTime expected = LocalDateTime.of(2021, 2, 6, 18, 0);
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }

    /**
     * 测试 YEARLY_MONTH 类型
     */
    @Test
    void testGetNextYearlyMonthEnd_CurrentYear() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("yearly_month")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .startMonth(3)
                .endMonth(6)
                .build();

        LocalDateTime now = LocalDateTime.of(2021, 1, 1, 9, 0);
        LocalDateTime expected = LocalDateTime.of(2021, 6, 1, 18, 0);
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }

    @Test
    void testGetNextYearlyMonthEnd_NextYear() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("yearly_month")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .startMonth(3)
                .endMonth(6)
                .build();

        LocalDateTime now = LocalDateTime.of(2021, 7, 1, 9, 0);
        LocalDateTime expected = LocalDateTime.of(2022, 6, 1, 18, 0);
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }

    /**
     * 测试 YEARLY_DATE_RANGE 类型
     */
    @Test
    void testGetNextYearlyDateRangeEnd_CurrentYear() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("yearly_date_range")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .startYearlyDate("12-25")
                .endYearlyDate("01-05")
                .build();

        LocalDateTime now = LocalDateTime.of(2021, 12, 25, 9, 0);
        LocalDateTime expected = LocalDateTime.of(2022, 1, 5, 18, 0);
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }

    @Test
    void testGetNextYearlyDateRangeEnd_NextYear() {
        TimeRangeDTO rule = TimeRangeDTO.builder()
                .type("yearly_date_range")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .startYearlyDate("12-25")
                .endYearlyDate("01-05")
                .build();

        LocalDateTime now = LocalDateTime.of(2022, 1, 5, 19, 0);
        LocalDateTime expected = LocalDateTime.of(2023, 1, 5, 18, 0);
        assertEquals(LocalDateTimeUtils.convertLocalDateTime2Timestamp(expected), TimeRangeUtil.getNextEndTimestamp(LocalDateTimeUtils.convertLocalDateTime2Timestamp(now), rule));
    }
}