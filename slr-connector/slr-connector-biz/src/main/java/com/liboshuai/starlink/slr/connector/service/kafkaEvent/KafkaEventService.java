package com.liboshuai.starlink.slr.connector.service.kafkaEvent;

import com.liboshuai.starlink.slr.connector.pojo.vo.kafkaEvent.KafkaEventGroupReqVO;
import com.liboshuai.starlink.slr.connector.pojo.vo.kafkaEvent.KafkaInfoRespVO;

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
