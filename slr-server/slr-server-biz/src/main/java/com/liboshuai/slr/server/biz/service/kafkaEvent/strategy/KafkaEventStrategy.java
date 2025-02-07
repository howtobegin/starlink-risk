package com.liboshuai.slr.server.biz.service.kafkaEvent.strategy;


import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;

import java.util.List;

/**
 * 上送事件策略抽象类
 */
public interface KafkaEventStrategy {
    /**
     * 数据的前置处理
     */
    void processAfter(List<KafkaEventDTO> kafkaEventDTOList);
}
