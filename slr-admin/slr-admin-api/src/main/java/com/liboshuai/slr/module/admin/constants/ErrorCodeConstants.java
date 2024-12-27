package com.liboshuai.slr.module.admin.constants;


import com.liboshuai.slr.framework.common.exception.ErrorCode;

/**
 * admin 错误码枚举类
 *
 * admin 系统，使用 20001-30000 段
 */
public interface ErrorCodeConstants {

    ErrorCode RULE_CODE_NOT_BLANK = new ErrorCode(20001, "规则编号[ruleCode]不能为空");
    ErrorCode RULE_INFO_NOT_EXISTS = new ErrorCode(20002, "规则编号[{}]对应的规则信息不存在");
    ErrorCode RULE_INFO_STATUS_NOT_DRAFT = new ErrorCode(20003, "规则编号[{}]对应的规则状态不为[草稿]，无法进行上线操作");
    ErrorCode RULE_INFO_STATUS_NOT_ONLINE_PENDING = new ErrorCode(20004, "规则编号[{}]对应的规则状态不为[上线待审核]，无法进行上线审核操作");
    ErrorCode RULE_INFO_STATUS_NOT_ONLINE = new ErrorCode(20005, "规则编号[{}]对应的规则状态不为[已上线]，无法进行下线操作");
    ErrorCode RULE_INFO_STATUS_NOT_OFFLINE_PENDING = new ErrorCode(20006, "规则编号[{}]对应的规则状态不为[下线待审核]，无法进行下线审核操作");

}
