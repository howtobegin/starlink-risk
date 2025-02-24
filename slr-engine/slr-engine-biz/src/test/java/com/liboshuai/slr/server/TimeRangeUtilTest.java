package com.liboshuai.slr.server;

import com.liboshuai.slr.engine.api.dto.TimeRangeDTO;
import com.liboshuai.slr.engine.api.enums.TimeRangeEnum;
import com.liboshuai.slr.engine.api.utils.TimeRangeUtil;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeRangeUtilTest {

    // ================= DAILY 测试 =================

    @Test
    void testIsWithinDaily_ValidCase() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.DAILY);
        rule.setStartTime(LocalTime.of(9, 0));  // 09:00
        rule.setEndTime(LocalTime.of(17, 0));   // 17:00

        LocalDateTime testTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)); // 10:00
        assertTrue(TimeRangeUtil.isWithinRule(testTime, rule));
    }

    @Test
    void testIsWithinDaily_BoundaryCase() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.DAILY);
        rule.setStartTime(LocalTime.of(9, 0));//09:00
        rule.setEndTime(LocalTime.of(17, 0));//17:00

        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)), rule));
        assertTrue(TimeRangeUtil.isWithinRule(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)), rule));
    }

    @Test
    void testIsWithinDaily_InvalidCase() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.DAILY);
        rule.setStartTime(LocalTime.of(9, 0)); // 09:00
        rule.setEndTime(LocalTime.of(17, 0)); // 17:00

        LocalDateTime testTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)); // 18:00
        assertFalse(TimeRangeUtil.isWithinRule(testTime, rule));
    }

    // ================= WEEKLY 测试 =================

    @Test
    void testIsWithinWeekly_ValidCase() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.WEEKLY);
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));
        rule.setDaysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        LocalDateTime testTime = LocalDateTime.of(LocalDate.of(2024, 6, 10), LocalTime.of(10, 0)); // 2024-06-10 (周一)
        assertTrue(TimeRangeUtil.isWithinRule(testTime, rule));
    }

    @Test
    void testIsWithinWeekly_InvalidDay() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.WEEKLY);
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));
        rule.setDaysOfWeek(Collections.singletonList(DayOfWeek.SATURDAY));

        LocalDateTime testTime = LocalDateTime.of(LocalDate.of(2024, 6, 10), LocalTime.of(10, 0)); // 2024-06-10 (周一)
        assertFalse(TimeRangeUtil.isWithinRule(testTime, rule));
    }

    // ================= MONTHLY 测试 =================

    @Test
    void testIsWithinMonthly_ValidCase() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.MONTHLY);
        rule.setStartDayOfMonth(10);
        rule.setEndDayOfMonth(20);
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));

        LocalDateTime testTime = LocalDateTime.of(LocalDate.of(2024, 6, 15), LocalTime.of(10, 0));
        assertTrue(TimeRangeUtil.isWithinRule(testTime, rule));
    }

    @Test
    void testIsWithinMonthly_OutOfRange() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.MONTHLY);
        rule.setStartDayOfMonth(10);
        rule.setEndDayOfMonth(20);
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));

        LocalDateTime testTime = LocalDateTime.of(LocalDate.of(2024, 6, 25), LocalTime.of(10, 0));
        assertFalse(TimeRangeUtil.isWithinRule(testTime, rule));
    }

    // ================= YEARLY_MONTH 测试 =================

    @Test
    void testIsWithinYearlyMonth_ValidCase() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.YEARLY_MONTH);
        rule.setStartMonth(3);
        rule.setEndMonth(6);
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));

        LocalDateTime testTime = LocalDateTime.of(LocalDate.of(2024, 4, 10), LocalTime.of(10, 0));
        assertTrue(TimeRangeUtil.isWithinRule(testTime, rule));
    }

    @Test
    void testIsWithinYearlyMonth_OutOfMonthRange() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.YEARLY_MONTH);
        rule.setStartMonth(3);
        rule.setEndMonth(6);
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));

        LocalDateTime testTime = LocalDateTime.of(LocalDate.of(2024, 2, 10), LocalTime.of(10, 0));
        assertFalse(TimeRangeUtil.isWithinRule(testTime, rule));
    }

    // ================= YEARLY_DATE_RANGE 测试 =================

    @Test
    void testIsWithinYearlyDateRange_ValidCase() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.YEARLY_DATE_RANGE);
        rule.setStartYearlyDate(MonthDay.of(12, 25));
        rule.setEndYearlyDate(MonthDay.of(1, 5));
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));

        LocalDateTime testTime = LocalDateTime.of(LocalDate.of(2024, 12, 30), LocalTime.of(10, 0));
        assertTrue(TimeRangeUtil.isWithinRule(testTime, rule));

        LocalDateTime testTime2 = LocalDateTime.of(LocalDate.of(2025, 1, 3), LocalTime.of(12, 0));
        assertTrue(TimeRangeUtil.isWithinRule(testTime2, rule));
    }

    @Test
    void testIsWithinYearlyDateRange_InvalidCase() {
        TimeRangeDTO rule = new TimeRangeDTO();
        rule.setType(TimeRangeEnum.YEARLY_DATE_RANGE);
        rule.setStartYearlyDate(MonthDay.of(12, 25));
        rule.setEndYearlyDate(MonthDay.of(1, 5));
        rule.setStartTime(LocalTime.of(9, 0));
        rule.setEndTime(LocalTime.of(17, 0));

        LocalDateTime testTime = LocalDateTime.of(LocalDate.of(2024, 2, 10), LocalTime.of(10, 0));
        assertFalse(TimeRangeUtil.isWithinRule(testTime, rule));
    }
}