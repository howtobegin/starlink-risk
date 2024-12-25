package com.liboshuai.slr.module.engine.framework.exception;

/**
 * 自定义业务异常
 */
public class BusinessException extends RuntimeException{
    private static final long serialVersionUID = 5891369618259637671L;

    public BusinessException(String message) {
        super(message);
    }
}
