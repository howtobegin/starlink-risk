package com.liboshuai.starlink.slr.engine.api.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 规则条件类型
 */
@Getter
public enum RuleCondTypeEnum {
    RANGE("RANGE", "范围"),
    PERIODIC("CYCLE", "周期")
    ;

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
}
