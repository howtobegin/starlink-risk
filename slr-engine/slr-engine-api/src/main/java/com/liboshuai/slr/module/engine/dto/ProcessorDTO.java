package com.liboshuai.slr.module.engine.dto;

import com.liboshuai.slr.module.engine.type.ProcessorDtoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * 运算机DTO对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeInfo(ProcessorDtoType.class)
public class ProcessorDTO implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 事件值累计结果
     * （key为事件字段，value为事件累计值）
     */
    private Map<String, Long> eventValueGroup;

}
