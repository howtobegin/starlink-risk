package com.liboshuai.starlink.slr.engine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 上送事件错误数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EventErrorDTO implements Serializable {

    private static final long serialVersionUID = 112687514700269556L;

    /**
     * 错误原因
     */
    private List<String> reasons;

    /**
     * 错误数据
     */
    private KafkaEventDTO kafkaEventDTO;

}
