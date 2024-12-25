package com.liboshuai.slr.module.engine.common.exception;

/**
 * author: liboshuai
 * description: 异常枚举类接口
 * date:  2023
 */
public interface BizExceptionInfo {

    /**
     * author: liboshuai
     * description: 获取异常错误码
     *
     * @param :
     * @return java.lang.String
     */
    String getExceptionCode();

    /**
     * author: liboshuai
     * description: 获取异常信息
     *
     * @param :
     * @return java.lang.String
     */
    String getExceptionMsg();
}
