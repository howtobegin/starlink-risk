package com.liboshuai.starlink.slr.connector.dao.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class KafkaEventListener {

    @KafkaListener(
            topics = "${slr-connector.kafka.consumer_topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void setCommitType(List<ConsumerRecord<String, Object>> consumerRecordList, Acknowledgment ack) {
        for (ConsumerRecord<String, Object> record : consumerRecordList) {
            // 打印消费的详细信息
            log.info("Consumed message - Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value());
        }
        // 手动提交偏移量
        ack.acknowledge();
    }
}
