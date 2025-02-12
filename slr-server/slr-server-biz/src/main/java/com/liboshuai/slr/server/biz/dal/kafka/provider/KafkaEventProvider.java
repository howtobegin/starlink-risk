package com.liboshuai.slr.server.biz.dal.kafka.provider;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.server.biz.framework.properties.KafkaProperties;
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
    public void batchSend(List<FlinkEventDTO> flinkEventDTOList) {
        String providerTopic = kafkaProperties.getEventTopic();
        if (CollectionUtils.isEmpty(flinkEventDTOList)) {
            return;
        }
        flinkEventDTOList.forEach(eventUploadDTO -> kafkaTemplate.send(
                providerTopic, generateKey(eventUploadDTO), eventUploadDTO)
        );
    }

    /**
     * 根据 channel、targetField 和 targetValue 生成消息的 key
     */
    private String generateKey(FlinkEventDTO eventUploadDTO) {
        String channel = eventUploadDTO.getChannel();
        String targetField = eventUploadDTO.getTargetField();
        String targetValue = eventUploadDTO.getTargetValue();
        // 使用下划线连接，避免键值冲突
        return String.join(DefaultConstants.COLON, channel, targetField, targetValue);
    }
}
