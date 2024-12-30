package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.utils.JsonUtil;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Objects;

public class KafkaEventProcessFunction extends ProcessFunction<String, KafkaEventDTO> {
    @Override
    public void processElement(String jsonValue, ProcessFunction<String, KafkaEventDTO>.Context context, Collector<KafkaEventDTO> collector) throws Exception {
        // 将json字符串转换为KafkaEventDTO对象
        KafkaEventDTO kafkaEventDTO = JsonUtil.parseObject(jsonValue, KafkaEventDTO.class);
        if (Objects.nonNull(kafkaEventDTO)) {
            // 获取当前处理时间
            long processingTime = context.timerService().currentProcessingTime();
            kafkaEventDTO.setEventTime(processingTime);
        }
        collector.collect(kafkaEventDTO);
    }
}
