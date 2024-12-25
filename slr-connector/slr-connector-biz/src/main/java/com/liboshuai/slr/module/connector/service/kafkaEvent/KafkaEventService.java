package com.liboshuai.slr.module.connector.service.kafkaEvent;

import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventGroupReqVO;
import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaInfoRespVO;

public interface KafkaEventService {

    /**
     * 业务平台上送事件数据到 kafka
     */
    void push(KafkaEventGroupReqVO kafkaEventGroupReqVO);

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    KafkaInfoRespVO kafkaInfo();
}
