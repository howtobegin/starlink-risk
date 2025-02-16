package com.liboshuai.slr.flume.interceptor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KafkaKeyInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(KafkaKeyInterceptor.class);

    @Override
    public void initialize() {
        log.info("初始化Kafka键拦截器");
    }

    @Override
    public Event intercept(Event event) {
        if (event == null) {
            log.warn("接收到空事件，跳过处理");
            return null;
        }

        try {
            String eventData = new String(event.getBody(), StandardCharsets.UTF_8);
            log.debug("正在处理事件数据：{}", eventData);

            // 使用 Gson 解析 JSON
            JsonObject jsonObject;
            try {
                jsonObject = JsonParser.parseString(eventData).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                log.error("无效的JSON格式：{}", eventData);
                return null;
            }

            // 直接提取字段
            String channel = getJsonStringField(jsonObject, "channel");
            String targetField = getJsonStringField(jsonObject, "targetField");
            String targetValue = getJsonStringField(jsonObject, "targetValue");

            if (StringUtils.isAnyEmpty(channel, targetField, targetValue)) {
                log.error("JSON中缺少必填字段。渠道: {}, 目标字段: {}, 目标值: {}",
                        channel, targetField, targetValue);
                return null;
            }

            String kafkaKey = String.format("%s::%s::%s", channel, targetField, targetValue);
            log.debug("生成的Kafka键：{}", kafkaKey);

            event.getHeaders().put("kafkaKey", kafkaKey);
            return event;

        } catch (Exception e) {
            log.error("处理事件时发生错误：{}", e.getMessage(), e);
            return null;
        }
    }

    private String getJsonStringField(JsonObject jsonObject, String fieldName) {
        if (jsonObject.has(fieldName) && !jsonObject.get(fieldName).isJsonNull()) {
            return jsonObject.get(fieldName).getAsString();
        }
        log.warn("字段缺失或为空：{}", fieldName);
        return null;
    }

    @Override
    public List<Event> intercept(List<Event> events) {
        List<Event> validEvents = new ArrayList<>();
        log.info("开始处理批量事件，数量：{}", events.size());

        for (Event event : events) {
            Event processed = intercept(event);
            if (processed != null) {
                validEvents.add(processed);
            }
        }

        log.info("处理完成。有效事件数：{}/{}", validEvents.size(), events.size());
        return validEvents;
    }

    @Override
    public void close() {
        log.info("关闭Kafka键拦截器");
    }

    public static class Builder implements Interceptor.Builder {
        @Override
        public Interceptor build() {
            return new KafkaKeyInterceptor();
        }

        @Override
        public void configure(Context context) {
            log.debug("正在配置Kafka键拦截器");
        }
    }
}