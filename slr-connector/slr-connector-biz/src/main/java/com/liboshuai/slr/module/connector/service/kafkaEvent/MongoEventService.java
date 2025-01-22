package com.liboshuai.slr.module.connector.service.kafkaEvent;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;

import java.util.List;

public interface MongoEventService {
    /**
     * 批量保存事件到MongoDB
     */
    void batchSaveEventToMongo(List<KafkaEventDTO> kafkaEventDTOList);
}
