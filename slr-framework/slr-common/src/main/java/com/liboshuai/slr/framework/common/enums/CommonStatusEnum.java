package com.liboshuai.slr.framework.common.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 通用状态枚举
 */
@Getter
public enum CommonStatusEnum implements StringArrayValuable {
    DRAFT("DRAFT", "草稿"),
    ONLINE_PENDING("ONLINE_PENDING", "上线待审核"),
    ONLINE("ONLINE", "已上线"),
    OFFLINE_PENDING("OFFLINE_PENDING", "下线待审核"),
    OFFLINE("OFFLINE", "已下线");

    public static final String[] ARRAYS = Arrays.stream(values()).map(CommonStatusEnum::getCode).toArray(String[]::new);

    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    CommonStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static CommonStatusEnum fromCode(String code) {
        for (CommonStatusEnum status : CommonStatusEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static CommonStatusEnum fromDesc(String desc) {
        for (CommonStatusEnum status : CommonStatusEnum.values()) {
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
