package com.liboshuai.slr.flume.interceptor;

import com.liboshuai.slr.engine.api.dto.NginxEventDTO;
import com.liboshuai.slr.framework.common.constants.RedisKeyConstants;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 获取 Kafka key 拦截器
 */
@Slf4j
public class KafkaKeyInterceptor implements Interceptor {
    @Override
    public void initialize() {

    }

    @Override
    public Event intercept(Event event) {
        if (event == null) {
            return null;
        }

        try {
            // 将事件的 body 从 byte 转换为 String
            String eventData = new String(event.getBody(), StandardCharsets.UTF_8);

            // 使用 Jackson 解析 JSON
            NginxEventDTO nginxEventDTO = JsonUtils.parseObject(eventData, NginxEventDTO.class);
            if (Objects.isNull(nginxEventDTO)) {
                return null;
            }

            // 获取需要的字段
            String channel = nginxEventDTO.getChannel();
            String targetField = nginxEventDTO.getTargetField();
            String targetValue = nginxEventDTO.getTargetValue();

            // 构建 key
            String kafkaKey = channel + RedisKeyConstants.REDIS_KEY_SPLIT + targetField + RedisKeyConstants.REDIS_KEY_SPLIT + targetValue;

            // 将生成的 key 设置为 event 的 header
            event.getHeaders().put("kafkaKey", kafkaKey);

            return event;
        } catch (Exception e) {
            log.error("处理事件数据错误: ", e);
            return null;
        }
    }

    @Override
    public List<Event> intercept(List<Event> list) {
        List<Event> eventList = new ArrayList<>();
        for (Event event : list) {
            Event intercept = intercept(event);
            if (Objects.nonNull(intercept)) {
                eventList.add(intercept);
            }
        }
        return eventList;
    }

    @Override
    public void close() {

    }
}
