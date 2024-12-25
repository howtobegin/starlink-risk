package com.liboshuai.slr.module.engine.common.exception;


/**
 * author: liboshuai
 * description: Flink 配置信息异常枚举类
 * date: 2023
 */
public enum FlinkPropertiesExceptionInfo implements BizExceptionInfo {

    PROPERTIES_NULL("-300", "配置参数不存在");

    private final String exceptionCode;
    private final String exceptionMsg;

    FlinkPropertiesExceptionInfo(
            String exceptionCode,
            String exceptionMsg) {
        this.exceptionCode = exceptionCode;
        this.exceptionMsg = exceptionMsg;
    }

    @Override
    public String getExceptionCode() {
        return exceptionCode;
    }

    @Override
    public String getExceptionMsg() {
        return exceptionMsg;
    }
}
