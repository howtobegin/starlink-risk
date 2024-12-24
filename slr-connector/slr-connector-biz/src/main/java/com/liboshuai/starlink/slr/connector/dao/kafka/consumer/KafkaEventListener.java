package com.liboshuai.starlink.slr.connector.dao.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaEventListener {

    @KafkaListener(
            topics = "${slr-connector.kafka.consumer_topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void setCommitType(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        log.info("消费kafka的消息");
        log.info("内容：" + record.value());
        log.info("分区：" + record.partition());
        log.info("偏移量：" + record.offset());
        log.info("创建消息的时间戳：" + record.timestamp());
        ack.acknowledge();
    }
}
