package com.liboshuai.slr.module.engine.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum RuleStatusEnum {
    DRAFT("DRAFT", "草稿"),
    ONLINE_PENDING("ONLINE_PENDING", "上线待审核"),
    ONLINE("ONLINE", "已上线"),
    OFFLINE_PENDING("OFFLINE_PENDING", "下线待审核"),
    OFFLINE("OFFLINE", "已下线");

    /**
     * 编码
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    RuleStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过代码查找枚举值
     */
    public static RuleStatusEnum fromCode(String code) {
        for (RuleStatusEnum status : RuleStatusEnum.values()) {
            if (Objects.equals(status.getCode(), code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的code: " + code);
    }

    /**
     * 通过描述查找枚举值
     */
    public static RuleStatusEnum fromDesc(String desc) {
        for (RuleStatusEnum status : RuleStatusEnum.values()) {
            if (status.getDesc().equals(desc)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的desc: " + desc);
    }
}
