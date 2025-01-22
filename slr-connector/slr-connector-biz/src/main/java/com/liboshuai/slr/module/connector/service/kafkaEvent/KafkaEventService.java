package com.liboshuai.slr.module.connector.service.kafkaEvent;

import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaEventGroupReqVO;
import com.liboshuai.slr.module.connector.controller.kafkaEvent.vo.KafkaInfoRespVO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;

import java.util.List;

public interface KafkaEventService {

    /**
     * 业务平台上送事件数据到 kafka
     */
    void push(KafkaEventGroupReqVO kafkaEventGroupReqVO);

    /**
     * 根据事件id列表查询kafka事件数据集合
     */
    List<KafkaEventDTO> selectListByEventIds(List<Long> eventIdList);

    /**
     * 获取Kafka信息，包含是否可连接，并获取broker列表、topic列表、消费组列表等
     */
    KafkaInfoRespVO kafkaInfo();

    /**
     * 批量保存事件到mongo
     */
    void batchSaveEventToMongo(List<KafkaEventDTO> kafkaEventDTOList);

    /**
     * 删除mongo中过期事件数据
     */
    void deleteOldEventFromMongo();
}
