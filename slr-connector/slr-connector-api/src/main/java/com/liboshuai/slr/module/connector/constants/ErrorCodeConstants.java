package com.liboshuai.slr.module.connector.constants;


import com.liboshuai.slr.framework.common.exception.ErrorCode;

/**
 * connector 错误码常量
 * {connector 系统，使用 10001-20000 段}
 */
public interface ErrorCodeConstants {

    ErrorCode UPLOAD_EVENT_MAJOR_ERROR = new ErrorCode(10001, "上送事件数据存在严重错误，所有数据放弃上送kafka");

    ErrorCode UPLOAD_EVENT_MINOR_ERROR = new ErrorCode(10002, "上送事件数据存在部分错误，错误数据放弃上送kafka");

    ErrorCode RULE_EVENT_GROUP_NULL = new ErrorCode(10003, "规则事件组为空");
}
