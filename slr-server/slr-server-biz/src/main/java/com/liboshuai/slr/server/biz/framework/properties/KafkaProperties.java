package com.liboshuai.slr.server.biz.framework.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * kafka配置
 */
@Data
@Component
public class KafkaProperties {
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${slr-connector.kafka.event.topic}")
    private String eventTopic;
    @Value("${slr-connector.kafka.event.partition}")
    private Integer eventPartition;
    @Value("${slr-connector.kafka.event.replica}")
    private Short eventReplica;
    @Value("${slr-connector.kafka.alert.topic}")
    private String alertTopic;
    @Value("${slr-connector.kafka.alert.partition}")
    private Integer alertPartition;
    @Value("${slr-connector.kafka.alert.replica}")
    private Short alertReplica;
}
