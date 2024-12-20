package com.liboshuai.starlink.slr.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
public class ProcessorDTO implements Serializable {
    private static final long serialVersionUID = -1L;

    /**
     * 事件值累计结果
     */
    private Map<String, Long> eventCodeAndValueSumMap;

}
