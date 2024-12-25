package com.liboshuai.slr.framework.common.exception;

import com.liboshuai.slr.framework.common.exception.enums.ServiceErrorCodeRange;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务逻辑异常 Exception
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 4645944812175958263L;

    /**
     * 业务错误码
     *
     * @see ServiceErrorCodeRange
     */
    private Integer code;
    /**
     * 错误提示
     */
    private String message;
    /**
     * 数据
     */
    private Object data;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException() {
    }

    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

    public ServiceException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServiceException(ErrorCode errorCode, Object data) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
        this.data = data;
    }

    public ServiceException(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
