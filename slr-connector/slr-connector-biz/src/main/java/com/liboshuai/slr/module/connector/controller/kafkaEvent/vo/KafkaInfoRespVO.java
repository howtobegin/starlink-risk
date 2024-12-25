package com.liboshuai.slr.module.connector.controller.kafkaEvent.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Kafka信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KafkaInfoRespVO implements Serializable {
    private static final long serialVersionUID = 1291549034802206760L;

    private String bootstrapServers;
    private boolean success;
    private String errorMessage;
    private List<String> brokers;
    private List<String> topics;
    private List<String> consumerGroups;
}
