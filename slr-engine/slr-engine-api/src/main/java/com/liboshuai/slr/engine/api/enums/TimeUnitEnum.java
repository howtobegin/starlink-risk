package com.liboshuai.slr.engine.api.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * 时间单位枚举
 */
@Getter
public enum TimeUnitEnum implements StringArrayValuable {

    MILLISECOND("毫秒"),
    SECOND("秒"),
    MINUTE("分钟"),
    HOUR("小时"),
    DAY("天"),
    WEEK("周"),
    MONTH("月"),
    YEAR("年");

    public static final String[] ARRAYS = Arrays.stream(values()).map(TimeUnitEnum::getValue).toArray(String[]::new);

    private final String value;

    TimeUnitEnum(String value) {
        this.value = value;
    }

    /**
     * 根据值获取枚举
     */
    public static TimeUnitEnum fromEnUnit(String value) {
        for (TimeUnitEnum timeUnitEnum : TimeUnitEnum.values()) {
            if (timeUnitEnum.getValue().equals(value)) {
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
