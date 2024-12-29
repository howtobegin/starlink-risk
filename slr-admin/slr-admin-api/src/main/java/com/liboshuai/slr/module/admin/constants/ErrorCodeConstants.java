package com.liboshuai.slr.module.admin.constants;


import com.liboshuai.slr.framework.common.exception.ErrorCode;

/**
 * admin 错误码枚举类
 *
 * admin 系统，使用 20001-30000 段
 */
public interface ErrorCodeConstants {

    ErrorCode RULE_CODE_NOT_BLANK = new ErrorCode(20001, "规则编号[ruleCode]不能为空");
    ErrorCode RULE_INFO_NOT_EXISTS = new ErrorCode(20002, "编号为[{}]的规则不存在");
    ErrorCode RULE_INFO_STATUS_NOT_DRAFT = new ErrorCode(20003, "编号为[{}]的规则状态不为[草稿/已下线]，无法进行上线操作");
    ErrorCode RULE_INFO_STATUS_NOT_ONLINE_PENDING = new ErrorCode(20004, "编号为[{}]的规则状态不为[上线待审核]，无法进行上线审核操作");
    ErrorCode RULE_INFO_STATUS_NOT_ONLINE = new ErrorCode(20005, "编号为[{}]的规则状态不为[已上线]，无法进行下线操作");
    ErrorCode RULE_INFO_STATUS_NOT_OFFLINE_PENDING = new ErrorCode(20006, "编号为[{}]的规则状态不为[下线待审核]，无法进行下线审核操作");
    ErrorCode RULE_INFO_AUDIT_OP_NOT_SUPPORT = new ErrorCode(20007, "编号为[{}]的规则进行审核操作时，审核操作符[auditOp]不能为空");
    ErrorCode RULE_INFO_NEW_STATUS_NOT_SUPPORT = new ErrorCode(20008, "编号为[{}]的规则新状态值错误");
    ErrorCode RULE_TARGET_CODE_EXISTS = new ErrorCode(20009, "规则目标编号[{}]已经存在了");
    ErrorCode RULE_EVENT_CODE_EXISTS = new ErrorCode(20010, "规则事件编号[{}]已经存在了");
    ErrorCode RULE_TARGET_ID_NOT_NULL = new ErrorCode(20011, "规则目标[id]不能为空");
    ErrorCode RULE_EVENT_NOT_NULL = new ErrorCode(20012, "规则事件不能为空");
    ErrorCode RULE_EVENT_ID_NOT_NULL = new ErrorCode(20013, "规则事件[id]不能为空");
    ErrorCode RULE_TARGET_ID_NOT_EXISTS = new ErrorCode(20014, "id为[{}]规则目标不存在");
    ErrorCode RULE_EVENT_ID_NOT_EXISTS = new ErrorCode(20015, "id为[{}]规则事件不存在");
    ErrorCode RULE_JSON_COUNT_SELECT_ERROR = new ErrorCode(20016, "查询规则编号[{}]的json数据数量失败");
    ErrorCode RULE_JSON_EXISTS = new ErrorCode(20017, "规则编号[{}]的json数据已经存在");
    ErrorCode RULE_COND_NOT_EXISTS = new ErrorCode(20018, "规则编号[{}]的规则条件不存在");
    ErrorCode RULE_CONDITION_EVENT_CODE_IS_NULL = new ErrorCode(20019, "规则条件中的规则事件编号为空");
    ErrorCode RULE_EVENT_NOT_EXISTS = new ErrorCode(20020, "编号为[{}]的规则事件不存在");
    ErrorCode RULE_EVENT_ATTR_NOT_EXISTS = new ErrorCode(20021, "规则事件编号[{}]的规则事件属性不存在");
    ErrorCode RULE_EVENT_ATTR_VALUE_NOT_EXISTS = new ErrorCode(20022, "规则事件编号[{}]的规则事件属性值不存在");
    ErrorCode RULE_CONDITION_COND_CODE_IS_NULL = new ErrorCode(20023, "规则条件中条件编号不能为空");
    ErrorCode RULE_MODEL_NOT_EXISTS = new ErrorCode(20024, "规则模型编号[{}]不存在");
    ErrorCode RULE_MODEL_CODE_IS_NULL = new ErrorCode(20025, "规则模型编号不能为空");
    ErrorCode RULE_MODEL_GROOVY_IS_NULL = new ErrorCode(20026, "规则模型编号[{}]的groovy脚本不能为空");
    ErrorCode RULE_TARGET_CODE_IS_NULL = new ErrorCode(20027, "规则目标编号不能为空");
    ErrorCode RULE_TARGET_NOT_EXISTS = new ErrorCode(20028, "规则目标编号[{}]不存在");
}
