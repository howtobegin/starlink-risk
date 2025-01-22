package com.liboshuai.slr.module.connector.controller.kafkaEvent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 业务平台上送事件数据到 kafka 入参
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KafkaEventGroupReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道", example = "GAME", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channel;

    @Schema(description = "事件组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<KafkaEventReqVO> kafkaEventGroup;
}
