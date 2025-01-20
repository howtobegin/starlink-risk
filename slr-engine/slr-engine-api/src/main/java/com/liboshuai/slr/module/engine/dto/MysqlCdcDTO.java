package com.liboshuai.slr.module.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MysqlCdcDTO implements Serializable {

    private static final long serialVersionUID = -5669551177148147064L;
    /**
     * 之前的数据
     */
    private String before;
    /**
     * 之后的数据
     */
    private String after;
    /**
     * 操作类型：r-查询；u-更新；d-删除；c-创建
     */
    private String op;

}
