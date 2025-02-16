package com.liboshuai.slr.engine.biz.function;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 补充事件数据缺失的字段数据
 */
public class FlinkEventProcessFunction extends ProcessFunction<FlinkEventDTO, FlinkEventDTO> {

    @Override
    public void processElement(FlinkEventDTO flinkEventDTO, ProcessFunction<FlinkEventDTO, FlinkEventDTO>.Context context, Collector<FlinkEventDTO> collector) throws Exception {
        // CEP之前的版本，都使用当前处理时间代替事件时间
        long currentProcessingTime = context.timerService().currentProcessingTime();
        flinkEventDTO.setEventTime(currentProcessingTime);
        collector.collect(flinkEventDTO);
    }
}
