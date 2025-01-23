package com.liboshuai.slr.module.connector.dal.kafka.provider;

import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.module.connector.framework.properties.KafkaProperties;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProvider {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    /**
     * 批量上送事件信息到kafka
     */
    public void batchSend(List<KafkaEventDTO> kafkaEventDTOList) {
        String providerTopic = kafkaProperties.getEventTopic();
        if (CollectionUtils.isEmpty(kafkaEventDTOList)) {
            return;
        }
        kafkaEventDTOList.forEach(eventUploadDTO -> kafkaTemplate.send(
                providerTopic, generateKey(eventUploadDTO), eventUploadDTO)
        );
    }

    /**
     * 根据 channel、targetField 和 targetValue 生成消息的 key
     */
    private String generateKey(KafkaEventDTO eventUploadDTO) {
        String channel = eventUploadDTO.getChannel();
        String targetField = eventUploadDTO.getTargetField();
        String targetValue = eventUploadDTO.getTargetValue();
        // 使用下划线连接，避免键值冲突
        return String.join(DefaultConstants.COLON, channel, targetField, targetValue);
    }
}
