package com.liboshuai.slr.engine.api.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 渠道枚举
 */
@Getter
public enum ChannelEnum implements StringArrayValuable {

    GAME("GAME", "游戏"),
    HJF("HJF", "花积分"),
    MALL("MALL", "商场"),
    CLM("CLM", "积分")
    ;

    public static final String[] ARRAYS = Arrays.stream(values()).map(ChannelEnum::getCode).toArray(String[]::new);

    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    ChannelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static ChannelEnum fromCode(String code) {
        for (ChannelEnum status : ChannelEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static ChannelEnum fromDesc(String desc) {
        for (ChannelEnum status : ChannelEnum.values()) {
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
