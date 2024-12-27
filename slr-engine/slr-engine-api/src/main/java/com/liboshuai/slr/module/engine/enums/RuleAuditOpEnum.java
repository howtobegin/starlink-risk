package com.liboshuai.slr.module.engine.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum RuleAuditOpEnum implements StringArrayValuable {
    APPROVE("APPROVE", "通过"),
    REJECT("REJECT", "拒绝");

    public static final String[] ARRAYS = Arrays.stream(values()).map(RuleAuditOpEnum::getCode).toArray(String[]::new);
    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    RuleAuditOpEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static RuleAuditOpEnum fromCode(String code) {
        for (RuleAuditOpEnum status : RuleAuditOpEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static RuleAuditOpEnum fromDesc(String desc) {
        for (RuleAuditOpEnum status : RuleAuditOpEnum.values()) {
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
