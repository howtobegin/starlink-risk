package com.liboshuai.starlink.slr.engine.api.enums;

import lombok.Getter;

@Getter
public enum RuleAuditOperatorEnum {
    APPROVE(0, "通过"),
    REJECT(1, "拒绝");

    // 审核操作码
    private final Integer code;

    // 审核操作描述
    private final String desc;

    RuleAuditOperatorEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 通过代码查找状态
    public static RuleAuditOperatorEnum fromCode(int code) {
        for (RuleAuditOperatorEnum status : RuleAuditOperatorEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态码: " + code);
    }

    // 通过描述查找状态
    public static RuleAuditOperatorEnum fromDescription(String description) {
        for (RuleAuditOperatorEnum status : RuleAuditOperatorEnum.values()) {
            if (status.getDesc().equals(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态描述: " + description);
    }
}
