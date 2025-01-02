package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

/**
 * 使用 RichParallelSourceFunction 实现并行定时触发事件的 SourceFunction
 */
@Slf4j
public class HeartbeatSource extends RichParallelSourceFunction<KafkaEventDTO> {


    // 标记数据源是否在运行，虽然不实现 cancel，但保持良好风格
    private volatile boolean isRunning = true;

    @Override
    public void run(SourceContext<KafkaEventDTO> ctx) throws Exception {
        while (isRunning) {
            KafkaEventDTO kafkaEventDTO = KafkaEventDTO.builder().heartbeat(true).build();
            ctx.collect(kafkaEventDTO);
            log.debug("触发器心跳数据......");
            // 休眠指定的时间间隔
            Thread.sleep(1000);
        }
    }

    /**
     * 如果确实不需要实现 cancel，可以选择不覆盖此方法，或者实现为空方法。
     * 但为了良好的资源管理和风格，建议至少设置 isRunning 标志。
     */
    @Override
    public void cancel() {
        isRunning = false;
        log.warn("PeriodicTriggerSource 已取消");
    }
}

