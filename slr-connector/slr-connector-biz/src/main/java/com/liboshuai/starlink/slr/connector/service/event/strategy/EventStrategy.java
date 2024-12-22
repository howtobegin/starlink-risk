package com.liboshuai.starlink.slr.connector.service.event.strategy;


import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;

import java.util.List;

/**
 * 上送事件策略抽象类
 */
public interface EventStrategy {
    /**
     * 数据的前置处理
     */
    void processAfter(List<KafkaEventDTO> kafkaEventDTOList);
}
