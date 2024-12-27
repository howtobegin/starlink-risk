package com.liboshuai.slr.module.admin.constants;


import com.liboshuai.slr.framework.common.exception.ErrorCode;

/**
 * admin 错误码枚举类
 *
 * admin 系统，使用 20001-30000 段
 */
public interface ErrorCodeConstants {

    ErrorCode RULE_CODE_NOT_BLANK = new ErrorCode(20001, "规则编号[ruleCode]不能为空");
    ErrorCode RULE_INFO_NOT_EXISTS = new ErrorCode(20002, "规则[{}]不存在");
    ErrorCode RULE_INFO_STATUS_NOT_DRAFT = new ErrorCode(20003, "规则[{}]状态不为[草稿/已下线]，无法进行上线操作");
    ErrorCode RULE_INFO_STATUS_NOT_ONLINE_PENDING = new ErrorCode(20004, "规则[{}]状态不为[上线待审核]，无法进行上线审核操作");
    ErrorCode RULE_INFO_STATUS_NOT_ONLINE = new ErrorCode(20005, "规则[{}]状态不为[已上线]，无法进行下线操作");
    ErrorCode RULE_INFO_STATUS_NOT_OFFLINE_PENDING = new ErrorCode(20006, "规则[{}]状态不为[下线待审核]，无法进行下线审核操作");
    ErrorCode RULE_INFO_AUDIT_OP_NOT_SUPPORT = new ErrorCode(20007, "规则[{}]进行审核操作时，审核操作符[auditOp]不能为空");
    ErrorCode RULE_INFO_NEW_STATUS_NOT_SUPPORT = new ErrorCode(20008, "规则[{}]新状态值错误");
    ErrorCode RULE_KEY_CODE_EXISTS = new ErrorCode(20009, "规则key编号[{}]已经存在了");
    ErrorCode RULE_EVENT_CODE_EXISTS = new ErrorCode(20010, "规则事件编号[{}]已经存在了");
    ErrorCode RULE_KEY_ID_NOT_NULL = new ErrorCode(20011, "规则key[id]不能为空");
    ErrorCode RULE_EVENT_NOT_NULL = new ErrorCode(20012, "规则事件不能为空");
    ErrorCode RULE_EVENT_ID_NOT_NULL = new ErrorCode(20013, "规则事件[id]不能为空");
    ErrorCode RULE_KEY_ID_NOT_EXISTS = new ErrorCode(20014, "id为[{}]规则目标不存在");
    ErrorCode RULE_EVENT_ID_NOT_EXISTS = new ErrorCode(20015, "id为[{}]规则事件不存在");
}
