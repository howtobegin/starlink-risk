package com.liboshuai.slr.module.admin.constants;


import com.liboshuai.slr.framework.common.exception.ErrorCode;

/**
 * admin 错误码枚举类
 *
 * admin 系统，使用 20001-30000 段
 */
public interface ErrorCodeConstants {

    ErrorCode RULE_INFO_NOT_EXISTS = new ErrorCode(20001, "规则编号[{}]对应的规则信息不存在");
    ErrorCode RULE_CONDITION_NOT_EXISTS = new ErrorCode(20002, "规则编号[{}]对应的规则条件不存在");
    ErrorCode EVENT_INFO_NOT_EXISTS = new ErrorCode(20003, "事件编号集合[{}]对应的事件信息不存在");
    ErrorCode RULE_COUNT_NOT_EXISTS = new ErrorCode(20004, "在线规则数量为空");
    ErrorCode WINDOW_UNIT_NOT_EXISTS = new ErrorCode(20005, "时间窗口单位 [{}] 不存在");

}
