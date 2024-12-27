package com.liboshuai.slr.module.engine.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 规则事件属性类型
 */
@Getter
public enum RuleEventAttrTypeEnum implements StringArrayValuable {
    BYTE("byte", "字节类型"),
    SHORT("short", "短整型"),
    INT("int", "整型"),
    LONG("long", "长整型"),
    FLOAT("float", "单精度浮点型"),
    DOUBLE("double", "双精度浮点型"),
    BOOLEAN("boolean", "布尔型"),
    CHAR("char", "字符型"),
    STRING("String", "字符串类型");

    public static final String[] ARRAYS = Arrays.stream(values()).map(RuleEventAttrTypeEnum::getType).toArray(String[]::new);

    /**
     * 类型
     */
    private final String type;
    /**
     * 描述
     */
    private final String desc;

    RuleEventAttrTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 通过类型查找枚举值
     */
    public static RuleEventAttrTypeEnum fromCode(String code) {
        for (RuleEventAttrTypeEnum status : RuleEventAttrTypeEnum.values()) {
            if (Objects.equals(status.getType(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static RuleEventAttrTypeEnum fromDesc(String desc) {
        for (RuleEventAttrTypeEnum status : RuleEventAttrTypeEnum.values()) {
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
