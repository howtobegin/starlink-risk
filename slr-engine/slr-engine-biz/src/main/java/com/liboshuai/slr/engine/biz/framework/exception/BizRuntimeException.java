package com.liboshuai.slr.engine.biz.framework.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * author: liboshuai
 * description: 自定义异常类的根类
 * date: 2023
 */
@Slf4j
public class BizRuntimeException extends RuntimeException {

    /**
     * author: liboshuai
     * description: 自定义异常类构造方法
     *
     * @param info: 自定义异常枚举对象
     */
    public BizRuntimeException(BizExceptionInfo info) {
        log.error(info.getExceptionMsg());
    }
}
