package com.liboshuai.starlink.slr.engine.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础DTO
 */
@Data
public class BaseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    private Long id;
    /**
     * 创建用户
     */
    private String creator;
    /**
     * 更新用户
     */
    private String updater;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;
}
