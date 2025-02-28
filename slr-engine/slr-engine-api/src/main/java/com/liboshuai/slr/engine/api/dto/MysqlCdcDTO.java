package com.liboshuai.slr.engine.api.dto;

import com.liboshuai.slr.engine.api.type.MysqlCdcDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(MysqlCdcDtoType.class)
public class MysqlCdcDTO implements Serializable {

    private static final long serialVersionUID = -5669551177148147064L;
    /**
     * 库名、表名
     */
    private CdcSourceDTO source;
    /**
     * MySql binlog 事件发生时间
     */
    private Integer tsSec;
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
