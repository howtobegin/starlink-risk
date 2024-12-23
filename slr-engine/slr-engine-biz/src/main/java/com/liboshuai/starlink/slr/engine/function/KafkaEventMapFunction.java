package com.liboshuai.starlink.slr.engine.function;

import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.starlink.slr.engine.utils.DateUtil;
import com.liboshuai.starlink.slr.engine.utils.JsonUtil;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.Objects;


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