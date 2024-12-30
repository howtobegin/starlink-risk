package com.liboshuai.slr.module.engine.function;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.FilterFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * KafkaEventFilterFunction类实现了FilterFunction接口，用于过滤Kafka事件数据
 */
@Slf4j
public class KafkaEventFilterFunction implements FilterFunction<KafkaEventDTO> {

    /**
     * 过滤方法实现
     *
     * @param event Kafka事件数据对象，用于检查是否满足过滤条件
     * @return 如果事件数据有效则返回true，否则返回false
     */
    @Override
    public boolean filter(KafkaEventDTO event) {
        log.warn("接收到来自kafka的事件数据：{}", event);

        if (Objects.isNull(event)) {
            log.warn("无效的 KafkaEventDTO 数据：为空！");
            return false;
        }

        // 创建一个列表来存储为空的字段名称
        List<String> nullFields = new ArrayList<>();

        // 检查各个字段是否为空
        if (event.getChannel() == null || event.getChannel().trim().isEmpty()) {
            nullFields.add("channel");
        }
        if (event.getTargetFiled() == null || event.getTargetFiled().trim().isEmpty()) {
            nullFields.add("targetFiled");
        }
        if (event.getTargetValue() == null || event.getTargetValue().trim().isEmpty()) {
            nullFields.add("targetValue");
        }
        if (event.getEventFiled() == null || event.getEventFiled().trim().isEmpty()) {
            nullFields.add("eventFiled");
        }
        if (event.getEventValue() == null || event.getEventValue().trim().isEmpty()) {
            nullFields.add("eventValue");
        }
        if (event.getEventAttrMap() == null || event.getEventAttrMap().isEmpty()) {
            nullFields.add("eventAttribute");
        }

        // 如果有任何字段为空，则记录日志并过滤掉该事件
        if (!nullFields.isEmpty()) {
            log.warn("无效的 KafkaEventDTO 数据：字段 {} 为 null 或为空。事件：{}", nullFields, event);
            return false;
        }

        // 所有必填字段均不为空，保留该事件
        return true;
    }
}