package com.liboshuai.slr.engine.api.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 预警项目编号
 */
@Getter
public enum AlertProjectNoEnum implements StringArrayValuable {
    MALL("mall", "商场"),
    HJF("hjf", "花积分"),
    GAME("game", "游戏"),
    CLM("clm", "积分");

    public static final String[] ARRAYS = Arrays.stream(values()).map(AlertProjectNoEnum::getCode).toArray(String[]::new);

    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    AlertProjectNoEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static AlertProjectNoEnum fromCode(String code) {
        for (AlertProjectNoEnum status : AlertProjectNoEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static AlertProjectNoEnum fromDesc(String desc) {
        for (AlertProjectNoEnum status : AlertProjectNoEnum.values()) {
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
