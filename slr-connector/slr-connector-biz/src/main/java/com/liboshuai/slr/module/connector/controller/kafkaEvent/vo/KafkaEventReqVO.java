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

    @Schema(description = "key编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyCode;

    @Schema(description = "key值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyValue;

    @Schema(description = "事件编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventCode;

    @Schema(description = "事件值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventValue;

    /**
     * {@link RuleEventAttrDTO}
     */
    @Schema(description = "属性", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> eventAttribute;

}
