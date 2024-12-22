package com.liboshuai.starlink.slr.connector.service.event.strategy;

import com.liboshuai.starlink.slr.connector.common.constants.DefaultConstants;
import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认事件上送策略类
 */
@Slf4j
@Component
@EventStrategyTag(channels = {DefaultConstants.DEFAULT_STRATEGY})
public class DefaultEventStrategy implements EventStrategy {
    /**
     * 数据的前置处理
     */
    @Override
    public void processAfter(List<KafkaEventDTO> kafkaEventDTOList) {
        // 暂时没有处理逻辑

    }
}
