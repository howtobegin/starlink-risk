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
public class CdcSourceDTO implements Serializable {

    private static final long serialVersionUID = 6984843086225743923L;

    /**
     * 数据库名
     */
    private String database;
    /**
     * 表名
     */
    private String table;

}
