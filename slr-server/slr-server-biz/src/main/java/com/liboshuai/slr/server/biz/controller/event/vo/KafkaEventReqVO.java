package com.liboshuai.slr.server.biz.controller.event.vo;

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

    @Schema(description = "事件时间（13位，毫秒级）", example = "1736732339769", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long eventTime;

    @Schema(description = "目标字段", example = "userId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetField;

    @Schema(description = "目标值", example = "U0000000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetValue;

    @Schema(description = "事件字段", example = "lottery", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventField;

    @Schema(description = "事件值", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventValue;

    @Schema(description = "事件属性", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> eventAttrMap;

}
