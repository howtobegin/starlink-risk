package com.liboshuai.slr.engine.api.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum TimeRangeEnum {
    DAILY("daily", "每日时间范围"),
    WEEKLY("weekly", "每周特定天的时间范围"),
    MONTHLY("monthly", "每月特定日期的时间范围"),
    YEARLY_MONTH("yearly_month", "每年特定月份的时间范围"),
    YEARLY_DATE_RANGE("yearly_date_range", "每年特定日期范围");

    private final String code;
    private final String desc;

    TimeRangeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static TimeRangeEnum fromCode(String code) {
        for (TimeRangeEnum type : TimeRangeEnum.values()) {
            if (Objects.equals(type.getCode(), code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的 code: " + code);
    }

    /**
     * 根据描述获取枚举
     */
    public static TimeRangeEnum fromDesc(String desc) {
        for (TimeRangeEnum type : TimeRangeEnum.values()) {
            if (type.getDesc().equals(desc)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的 desc: " + desc);
    }
}