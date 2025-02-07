package com.liboshuai.slr.server.biz.service.kafkaEvent;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.server.biz.controller.kafkaEvent.vo.KafkaEventGroupReqVO;
import com.liboshuai.slr.server.biz.controller.kafkaEvent.vo.KafkaInfoRespVO;

import java.util.List;

public interface KafkaEventService {

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    KafkaInfoRespVO kafkaInfo();

    void createKafkaTopic(String bootstrapServers, String topicName, Integer partition, Short replica);

    /**
     * 业务平台上送事件数据到 kafka
     */
    void push(KafkaEventGroupReqVO kafkaEventGroupReqVO);

    /**
     * 根据事件id列表查询kafka事件数据集合
     */
    List<KafkaEventDTO> selectListByEventIds(List<Long> eventIdList);

    /**
     * 删除mongo中过期事件数据
     */
    void deleteOldEventFromMongo();
}
