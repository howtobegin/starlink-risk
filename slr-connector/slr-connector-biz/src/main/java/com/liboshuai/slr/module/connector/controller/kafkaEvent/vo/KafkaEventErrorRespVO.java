package com.liboshuai.slr.module.connector.controller.kafkaEvent.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 上送事件错误响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KafkaEventErrorRespVO implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * 数据索引
     */
    private Integer index;

    /**
     * 错误原因
     */
    private List<String> reasons;

    /**
     * 错误数据
     */
    private KafkaEventReqVO kafkaEventReqVO;

}
