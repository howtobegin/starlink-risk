package com.liboshuai.slr.server.biz.service.kafkaEvent;

import com.liboshuai.slr.engine.api.dto.KafkaEventDTO;

import java.util.List;

public interface MongoEventService {
    /**
     * 批量保存事件到MongoDB
     */
    void batchSaveEventToMongo(List<KafkaEventDTO> kafkaEventDTOList);
}
