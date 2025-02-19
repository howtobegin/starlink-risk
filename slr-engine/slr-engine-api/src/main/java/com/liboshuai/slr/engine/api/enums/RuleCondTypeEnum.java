package com.liboshuai.slr.engine.api.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 规则条件类型
 */
@Getter
public enum RuleCondTypeEnum implements StringArrayValuable {
    CRON("cron", "cron表达式类型"),
    RECENT("recent", "最近时间类型")
    ;

    public static final String[] ARRAYS = Arrays.stream(values()).map(RuleCondTypeEnum::getCode).toArray(String[]::new);

    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    RuleCondTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static RuleCondTypeEnum fromCode(String code) {
        for (RuleCondTypeEnum status : RuleCondTypeEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static RuleCondTypeEnum fromDesc(String desc) {
        for (RuleCondTypeEnum status : RuleCondTypeEnum.values()) {
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
