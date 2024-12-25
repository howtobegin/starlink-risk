package com.liboshuai.slr.module.connector.service.kafkaEvent.strategy;

import com.liboshuai.slr.module.connector.common.constants.DefaultConstants;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认事件上送策略类
 */
@Slf4j
@Component
@KafkaEventStrategyTag(channels = {DefaultConstants.DEFAULT_STRATEGY})
public class DefaultKafkaEventStrategy implements KafkaEventStrategy {
    /**
     * 数据的前置处理
     */
    @Override
    public void processAfter(List<KafkaEventDTO> kafkaEventDTOList) {
        // 暂时没有处理逻辑

    }
}
