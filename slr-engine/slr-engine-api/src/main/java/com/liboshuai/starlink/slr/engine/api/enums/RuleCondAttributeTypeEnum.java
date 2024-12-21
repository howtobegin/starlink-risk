package com.liboshuai.starlink.slr.engine.api.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 规则条件属性类型
 */
@Getter
public enum RuleCondAttributeTypeEnum {
    BYTE("byte", "字节类型"),
    SHORT("short", "短整型"),
    INT("int", "整型"),
    LONG("long", "长整型"),
    FLOAT("float", "单精度浮点型"),
    DOUBLE("double", "双精度浮点型"),
    BOOLEAN("boolean", "布尔型"),
    CHAR("char", "字符型"),
    STRING("String", "字符串类型");

    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    RuleCondAttributeTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static RuleCondAttributeTypeEnum fromCode(String code) {
        for (RuleCondAttributeTypeEnum status : RuleCondAttributeTypeEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static RuleCondAttributeTypeEnum fromDesc(String desc) {
        for (RuleCondAttributeTypeEnum status : RuleCondAttributeTypeEnum.values()) {
            if (status.getDesc().equals(desc)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的desc: " + desc);
    }
}
