package com.liboshuai.slr.engine.biz.function;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.UUID;

/**
 * 补充事件数据缺失的字段数据
 */
public class FlinkEventProcessFunction extends ProcessFunction<FlinkEventDTO, FlinkEventDTO> {

    @Override
    public void processElement(FlinkEventDTO flinkEventDTO, ProcessFunction<FlinkEventDTO, FlinkEventDTO>.Context context, Collector<FlinkEventDTO> collector) throws Exception {
        // 设置事件时间为Flink当前处理时间
        long currentProcessingTime = context.timerService().currentProcessingTime();
        flinkEventDTO.setEventTime(currentProcessingTime);
        // 设置事件ID
        flinkEventDTO.setEventId(UUID.randomUUID().toString());
        collector.collect(flinkEventDTO);
    }
}
