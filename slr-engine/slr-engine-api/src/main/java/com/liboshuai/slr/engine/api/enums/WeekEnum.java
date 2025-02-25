package com.liboshuai.slr.engine.api.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum WeekEnum {
    MONDAY("monday", "周一"),
    TUESDAY("tuesday", "周二"),
    WEDNESDAY("wednesday", "周三"),
    THURSDAY("thursday", "周四"),
    FRIDAY("friday", "周五"),
    SATURDAY("saturday", "周六"),
    SUNDAY("sunday", "周日"),
    ;

    private final String code;
    private final String desc;

    WeekEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static WeekEnum fromCode(String code) {
        for (WeekEnum type : WeekEnum.values()) {
            if (Objects.equals(type.getCode(), code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的 code: " + code);
    }

    /**
     * 根据描述获取枚举
     */
    public static WeekEnum fromDesc(String desc) {
        for (WeekEnum type : WeekEnum.values()) {
            if (type.getDesc().equals(desc)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的 desc: " + desc);
    }
}