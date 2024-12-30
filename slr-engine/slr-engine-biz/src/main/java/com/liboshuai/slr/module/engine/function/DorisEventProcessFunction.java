package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.module.engine.convert.EventConvert;
import com.liboshuai.slr.module.engine.dto.DorisEventDTO;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.utils.DateUtil;
import com.liboshuai.slr.module.engine.utils.JsonUtil;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class DorisEventProcessFunction extends ProcessFunction<KafkaEventDTO, String> {

    @Override
    public void processElement(KafkaEventDTO kafkaEventDTO, ProcessFunction<KafkaEventDTO, String>.Context context, Collector<String> collector) throws Exception {
        DorisEventDTO dorisEventDTO = EventConvert.INSTANCE.kafkaDto2DorisDto(kafkaEventDTO);
        // flink当前处理时间设置事件时间
        // （虽然doris存储时使用的时间比规则引擎使用的处理时间要早一些，也就大概0.1秒~1秒之间，但是因为doris存储的精度只有秒级别，所有这些偏差都可以忽略）
        long currentProcessingTime = context.timerService().currentProcessingTime();
        String eventTime = DateUtil.convertTimestamp2String(currentProcessingTime);
        dorisEventDTO.setEventTime(eventTime);
        collector.collect(JsonUtil.toJsonStringWithUpperSnakeCaseKeys(dorisEventDTO));// 转为大写下划线，适配doris表结构字段
    }
}
