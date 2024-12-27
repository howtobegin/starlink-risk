package com.liboshuai.slr.module.engine.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 时间单位枚举
 */
@Getter
public enum TimeUnitEnum implements StringArrayValuable {

    MILLISECOND("MILLISECOND", "毫秒"),
    SECOND("SECOND", "秒"),
    MINUTE("MINUTE", "分钟"),
    HOUR("HOUR", "小时"),
    DAY("DAY", "天"),
    WEEK("WEEK", "周"),
    MONTH("MONTH", "月"),
    YEAR("YEAR", "年");

    public static final String[] ARRAYS = Arrays.stream(values()).map(TimeUnitEnum::getEnUnit).toArray(String[]::new);

    private final String enUnit;
    private final String cnUnit;

    TimeUnitEnum(String enUnit, String cnUnit) {
        this.enUnit = enUnit;
        this.cnUnit = cnUnit;
    }

    /**
     * 将英文单位转为中文单位
     * @param enUnit 英文单位
     * @return 中文单位
     */
    public static String convertEnUnitToCnUnit(String enUnit) {
        for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
            if (timeUnitEnum.getEnUnit().equals(enUnit)) {
                return timeUnitEnum.getCnUnit();
            }
        }
        return "未知单位";
    }

    /**
     * 将中文单位转为英文单位
     * @param cnUnit 中文单位
     * @return 英文单位
     */
    public static String convertCnUnitToEnUnit(String cnUnit) {
        for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
            if (timeUnitEnum.getCnUnit().equals(cnUnit)) {
                return timeUnitEnum.getEnUnit();
            }
        }
        return "未知单位";
    }

    /**
     * 根据中文单位获取枚举
     * @param cnUnit 中文单位
     * @return 枚举
     */
    public static TimeUnitEnum fromCnUnit(String cnUnit) {
        for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
            if (timeUnitEnum.getCnUnit().equals(cnUnit)) {
                return timeUnitEnum;
            }
        }
        return null;
    }

    /**
     * 根据英文单位获取枚举
     * @param enUnit 英文单位
     * @return 枚举
     */
    public static TimeUnitEnum fromEnUnit(String enUnit) {
        for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
            if (timeUnitEnum.getEnUnit().equals(enUnit)) {
                return timeUnitEnum;
            }
        }
        return null;
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
