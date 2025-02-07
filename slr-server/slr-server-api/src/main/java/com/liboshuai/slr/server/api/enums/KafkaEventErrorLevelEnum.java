package com.liboshuai.slr.server.api.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * kafka事件数据错误级别
 */
@Getter
public enum KafkaEventErrorLevelEnum implements StringArrayValuable {
    MAJOR("major", "严重错误"),
    MINOR("minor", "部分错误");

    public static final String[] ARRAYS = Arrays.stream(values()).map(KafkaEventErrorLevelEnum::getCode).toArray(String[]::new);

    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    KafkaEventErrorLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static KafkaEventErrorLevelEnum fromCode(String code) {
        for (KafkaEventErrorLevelEnum status : KafkaEventErrorLevelEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static KafkaEventErrorLevelEnum fromDesc(String desc) {
        for (KafkaEventErrorLevelEnum status : KafkaEventErrorLevelEnum.values()) {
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
