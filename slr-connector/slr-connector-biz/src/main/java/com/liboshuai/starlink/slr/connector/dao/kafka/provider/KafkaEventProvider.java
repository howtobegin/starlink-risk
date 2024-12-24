package com.liboshuai.starlink.slr.connector.dao.kafka.provider;

import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class KafkaEventProvider {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${slr-connector.kafka.source_topic}")
    private String sourceTopic;

    /**
     * 批量上送事件信息到kafka
     */
    public void batchSend(List<KafkaEventDTO> kafkaEventDTOList) {
        if (CollectionUtils.isEmpty(kafkaEventDTOList)) {
            return;
        }
        kafkaEventDTOList.forEach(eventUploadDTO -> kafkaTemplate.send(sourceTopic, eventUploadDTO)
                .addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

                    @Override
                    public void onSuccess(SendResult<String, Object> result) {

                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("生产者发送消息：{} 失败，原因：{}", eventUploadDTO, ex.getMessage());
                    }
                }));
    }

    public void mockEventToKafka(KafkaEventDTO kafkaEventDTO) {
        kafkaTemplate.send(sourceTopic, kafkaEventDTO)
                .addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

                    @Override
                    public void onSuccess(SendResult<String, Object> result) {

                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("生产者发送消息：{} 失败，原因：{}", kafkaEventDTO, ex.getMessage());
                    }
                });
    }
}
