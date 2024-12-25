package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.module.engine.common.utils.DateUtil;
import com.liboshuai.slr.module.engine.common.utils.JsonUtil;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.Objects;

/**
 * 实现MapFunction接口，用于将字符串类型的消息映射为KafkaEventDTO对象
 * 主要用途是在Kafka消息消费过程中，将接收到的JSON字符串消息转换为KafkaEventDTO对象，并设置当前事件时间
 */
public class KafkaEventMapFunction implements MapFunction<String, KafkaEventDTO> {

    @Override
    public KafkaEventDTO map(String s) throws Exception {
        // 将json字符串转换为KafkaEventDTO对象
        KafkaEventDTO kafkaEventDTO = JsonUtil.parseObject(s, KafkaEventDTO.class);
        if (Objects.nonNull(kafkaEventDTO)) {
            // 设置事件时间
            kafkaEventDTO.setEventTime(DateUtil.convertTimestamp2String(System.currentTimeMillis()));
        }
        return kafkaEventDTO;
    }
}