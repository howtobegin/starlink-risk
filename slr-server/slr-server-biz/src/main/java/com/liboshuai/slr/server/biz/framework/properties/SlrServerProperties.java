package com.liboshuai.slr.server.biz.framework.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 项目配置
 */
@Data
@Component
public class SlrServerProperties {
    @Value("${slr-server.active}")
    private String active;

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${slr-server.kafka.event.topic}")
    private String eventTopic;
    @Value("${slr-server.kafka.event.partition}")
    private Integer eventPartition;
    @Value("${slr-server.kafka.event.replica}")
    private Short eventReplica;
    @Value("${slr-server.kafka.alert.topic}")
    private String alertTopic;
    @Value("${slr-server.kafka.alert.partition}")
    private Integer alertPartition;
    @Value("${slr-server.kafka.alert.replica}")
    private Short alertReplica;

    @Value("${slr-server.file-path.event-log}")
    private String eventLogFilePath;

    @Value("${slr-server.api-address.rso-alert}")
    private String rsoAlertApiAddress;
}
