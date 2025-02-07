package com.liboshuai.slr.engine.api.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 预警等级
 */
@Getter
public enum AlertLevelEnum implements StringArrayValuable {
    LEVEL_I("1", "I级预警"),
    LEVEL_II("2", "II级预警"),
    LEVEL_III("3", "III级预警"),
    LEVEL_IV("4", "IV级预警");

    public static final String[] ARRAYS = Arrays.stream(values()).map(AlertLevelEnum::getCode).toArray(String[]::new);

    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    AlertLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static AlertLevelEnum fromCode(String code) {
        for (AlertLevelEnum status : AlertLevelEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static AlertLevelEnum fromDesc(String desc) {
        for (AlertLevelEnum status : AlertLevelEnum.values()) {
            if (status.getDesc().equals(desc)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的desc: " + desc);
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
