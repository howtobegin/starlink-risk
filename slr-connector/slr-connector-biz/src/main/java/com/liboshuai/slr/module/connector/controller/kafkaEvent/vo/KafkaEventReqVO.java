package com.liboshuai.slr.module.connector.controller.kafkaEvent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 上送事件Kafka 入参
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KafkaEventReqVO implements Serializable {

    private static final long serialVersionUID = 1;

    @Schema(description = "目标字段", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetField;

    @Schema(description = "目标值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetValue;

    @Schema(description = "事件字段", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventField;

    @Schema(description = "事件值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventValue;

    @Schema(description = "事件属性", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> eventAttrMap;

}
