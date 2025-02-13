package com.liboshuai.slr.server.biz.service.event;

import com.liboshuai.slr.server.biz.controller.event.vo.KafkaEventGroupReqVO;
import com.liboshuai.slr.server.biz.controller.event.vo.KafkaInfoRespVO;

public interface EventService {

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    KafkaInfoRespVO kafkaInfo();

    void createKafkaTopic(String bootstrapServers, String topicName, Integer partition, Short replica);

    /**
     * 业务平台上送事件数据到 kafka
     */
    void push(KafkaEventGroupReqVO kafkaEventGroupReqVO);

}
