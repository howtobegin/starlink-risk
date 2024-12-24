package com.liboshuai.starlink.slr.connector.service.kafkaEvent;

import com.liboshuai.starlink.slr.connector.pojo.dto.kafkaEvent.KafkaEventGroupDTO;
import com.liboshuai.starlink.slr.connector.pojo.vo.kafkaEvent.KafkaInfoVO;

public interface KafkaEventService {

    /**
     * 上送事件数据到kafka
     */
    void push(KafkaEventGroupDTO kafkaEventGroupDTO);

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    KafkaInfoVO kafkaInfo();
}
