package com.liboshuai.slr.framework.common.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 通用审批操作枚举
 */
@Getter
public enum CommonAuditOpEnum implements StringArrayValuable {
    APPROVE("APPROVE", "通过"),
    REJECT("REJECT", "拒绝");

    public static final String[] ARRAYS = Arrays.stream(values()).map(CommonAuditOpEnum::getCode).toArray(String[]::new);
    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    CommonAuditOpEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static CommonAuditOpEnum fromCode(String code) {
        for (CommonAuditOpEnum status : CommonAuditOpEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static CommonAuditOpEnum fromDesc(String desc) {
        for (CommonAuditOpEnum status : CommonAuditOpEnum.values()) {
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
