package com.liboshuai.slr.module.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BankDTO implements Serializable {

    private static final long serialVersionUID = 6984843196225743923L;

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 卡系统版本
     */
    private String version;
    /**
     * 生产库名
     */
    private String database;
    /**
     * 银行名称
     */
    private String name;
    /**
     * 银行号
     */
    private String bank;
    /**
     * 银行机构号
     */
    private String institution;
    /**
     * 积分系统数据库
     */
    private String clmDatabase;
    /**
     * 银行所属省份地区
     */
    private String bankProvince;
    /**
     * 银行类型：0-全国性银行；区域性银行-1（默认）
     */
    private String bankType;
    /**
     * 银行首字母
     */
    private String initals;
    /**
     * 逻辑删除标记：0-正常，1-删除
     */
    private String isDeleted;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改人
     */
    private String updateUser;
    /**
     * 修改时间
     */
    private Date updateTime;
}
