package com.liboshuai.slr.flume.interceptor;

import com.liboshuai.slr.engine.api.dto.NginxEventDTO;
import com.liboshuai.slr.framework.common.constants.RedisKeyConstants;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Kafka Key 拦截器，用于从事件数据中提取键
 */
@Slf4j
public class KafkaKeyInterceptor implements Interceptor {

    @Override
    public void initialize() {
        // 此拦截器不需要初始化
    }

    @Override
    public Event intercept(Event event) {
        if (event == null) {
            log.warn("收到空的事件，跳过拦截处理。");
            return null;
        }

        try {
            // 将事件的 body 从 byte[] 转换为 String
            String eventData = new String(event.getBody(), StandardCharsets.UTF_8);
            log.debug("正在处理事件数据: {}", eventData);

            // 将事件数据解析为 JSON 的 NginxEventDTO 对象
            NginxEventDTO nginxEventDTO = JsonUtils.parseObject(eventData, NginxEventDTO.class);
            if (nginxEventDTO == null) {
                log.error("解析事件数据到 JSON 失败。事件数据: {}", eventData);
                return null;
            }

            // 提取需要的字段
            String channel = nginxEventDTO.getChannel();
            String targetField = nginxEventDTO.getTargetField();
            String targetValue = nginxEventDTO.getTargetValue();

            if (StringUtils.isAnyEmpty(channel, targetField, targetValue)) {
                log.error("事件数据中某些字段为空或缺失。nginxEventDTO: {}", nginxEventDTO);
                return null;
            }

            // 构建 Kafka 键
            String kafkaKey = channel + RedisKeyConstants.REDIS_KEY_SPLIT + targetField + RedisKeyConstants.REDIS_KEY_SPLIT + targetValue;
            log.debug("生成的 Kafka 键: {}", kafkaKey);

            // 将生成的键添加到事件的头部
            event.getHeaders().put("kafkaKey", kafkaKey);

            return event;
        } catch (Exception e) {
            log.error("处理事件数据时发生错误。异常信息: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        List<Event> interceptedEvents = new ArrayList<>();
        for (Event event : events) {
            Event intercepted = intercept(event);
            if (intercepted != null) {
                interceptedEvents.add(intercepted);
            }
        }
        log.debug("拦截了 {} 个事件，共处理了 {} 个事件。", interceptedEvents.size(), events.size());
        return interceptedEvents;
    }

    @Override
    public void close() {
        // 此拦截器不需要释放资源
    }

    public static class Builder implements Interceptor.Builder {

        @Override
        public Interceptor build() {
            return new KafkaKeyInterceptor();
        }

        @Override
        public void configure(Context context) {
            // 此拦截器不需要配置
        }
    }
}
