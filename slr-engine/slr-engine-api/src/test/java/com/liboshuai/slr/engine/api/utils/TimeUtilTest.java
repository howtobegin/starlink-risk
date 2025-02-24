package com.liboshuai.slr.engine.api.utils;

import com.liboshuai.slr.engine.api.enums.TimeUnitEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeUtilTest {

    @Test
    void testMillisConversion() {
        assertEquals(1000, TimeUtil.toMillis(1000, TimeUnitEnum.MILLISECOND));
    }

    @Test
    void testSecondsConversion() {
        assertEquals(5000, TimeUtil.toMillis(5, TimeUnitEnum.SECOND));
    }

    @Test
    void testMinutesConversion() {
        assertEquals(60000, TimeUtil.toMillis(1, TimeUnitEnum.MINUTE));
    }

    @Test
    void testHoursConversion() {
        assertEquals(3_600_000, TimeUtil.toMillis(1, TimeUnitEnum.HOUR));
    }

    @Test
    void testDaysConversion() {
        assertEquals(86_400_000, TimeUtil.toMillis(1, TimeUnitEnum.DAY));
    }

    @Test
    void testWeeksConversion() {
        assertEquals(604_800_000, TimeUtil.toMillis(1, TimeUnitEnum.WEEK));
    }

    @Test
    void testMonthsConversion() {
        assertEquals(2_592_000_000L, TimeUtil.toMillis(1, TimeUnitEnum.MONTH));
    }

    @Test
    void testYearsConversion() {
        assertEquals(31_536_000_000L, TimeUtil.toMillis(1, TimeUnitEnum.YEAR));
    }

    @Test
    void testNullUnit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                TimeUtil.toMillis(10, null)
        );
        assertEquals("Invalid windowSizeUnit is null！", exception.getMessage());
    }
}