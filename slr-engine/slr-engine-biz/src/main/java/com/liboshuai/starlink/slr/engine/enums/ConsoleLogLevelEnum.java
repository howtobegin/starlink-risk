package com.liboshuai.starlink.slr.engine.enums;

/**
 * @Author liboshuai
 * @Date 2023/11/6 1:37
 * 控制台日志打印工具类级别枚举
 */
public enum ConsoleLogLevelEnum {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF;

    public static ConsoleLogLevelEnum fromString(String levelStr) {
        for (ConsoleLogLevelEnum level : ConsoleLogLevelEnum.values()) {
            if (level.name().equalsIgnoreCase(levelStr)) {
                return level;
            }
        }
        return INFO; // Default level
    }
}
