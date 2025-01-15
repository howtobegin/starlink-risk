package com.liboshuai.slr.module.connector.rest.rsoAlarm.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RsoAlarmResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码 0 成功 -1 失败
     */
    private Integer status;

    /**
     * 响应消息
     */
    private String msg;
}
