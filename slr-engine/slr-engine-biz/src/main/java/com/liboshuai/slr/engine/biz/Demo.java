package com.liboshuai.slr.engine.biz;

import com.liboshuai.slr.engine.api.dto.TimeRangeDTO;
import com.liboshuai.slr.engine.api.enums.TimeRangeEnum;
import com.liboshuai.slr.engine.api.utils.TimeRangeUtil;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

public class Demo {
    public static void main(String[] args) {
        // 假定当前时间
        LocalDateTime now = LocalDateTime.of(2023, 10, 5, 9, 30);


        // 2. 构建一个“每周一和周三 10:00 - 12:00”的规则
        TimeRangeDTO weeklyRule = new TimeRangeDTO();
        weeklyRule.setType(TimeRangeEnum.WEEKLY);
        weeklyRule.setDaysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        weeklyRule.setStartTime(LocalTime.of(10, 0));
        weeklyRule.setEndTime(LocalTime.of(12, 0));

        // 执行判断：当前时间是否在这两个规则任意一个之内
        boolean result = TimeRangeUtil.isWithinRule(now, weeklyRule);
        System.out.println("是否匹配任意规则? " + result);
    }
}