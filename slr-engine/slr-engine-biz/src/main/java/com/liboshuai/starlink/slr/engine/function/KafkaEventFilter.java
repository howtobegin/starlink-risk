package com.liboshuai.starlink.slr.engine.function;

import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.FilterFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 过滤 kafkaEvent 中非法数据
 */
@Slf4j
public class KafkaEventFilter implements FilterFunction<KafkaEventDTO> {

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
        if (event.getKeyCode() == null || event.getKeyCode().trim().isEmpty()) {
            nullFields.add("keyCode");
        }
        if (event.getKeyValue() == null || event.getKeyValue().trim().isEmpty()) {
            nullFields.add("keyValue");
        }
        if (event.getEventCode() == null || event.getEventCode().trim().isEmpty()) {
            nullFields.add("eventCode");
        }
        if (event.getEventValue() == null || event.getEventValue().trim().isEmpty()) {
            nullFields.add("eventValue");
        }
        if (event.getEventAttribute() == null || event.getEventAttribute().isEmpty()) {
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