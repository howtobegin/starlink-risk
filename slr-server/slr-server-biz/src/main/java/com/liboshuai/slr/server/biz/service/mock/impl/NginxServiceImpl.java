package com.liboshuai.slr.server.biz.service.mock.impl;

import com.liboshuai.slr.engine.api.dto.EventDTO;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.server.biz.service.mock.NginxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NginxServiceImpl implements NginxService {

    private final RestTemplate restTemplate;

    /**
     * 测试 Nginx 后端请求
     */
    @Override
    public void testNginxBackendRequest() {
        // 模拟业务系统产生事件数据
        EventDTO eventData = createEventData();
        // 向打点服务器发送请求（示例代码为同步调用，生产实践推荐修改为异步调用）
        sendEventRequest(eventData);
    }

    /**
     * 模拟业务系统产生事件数据
     *
     * @return 构建好的 KafkaEventDTO 对象
     */
    public EventDTO createEventData() {
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("campaignId", "C000000004");
        eventAttributes.put("campaignName", "活动4");
        eventAttributes.put("bankName", "邮储银行");
        eventAttributes.put("bankNo", "6100");

        return EventDTO.builder()
                .channel("game")
                .targetField("userId")
                .targetValue("U000000002")
                .eventField("lottery")
                .eventValue("1")
                .eventAttrMap(eventAttributes)
                .build();
    }

    /**
     * 向打点服务器发送请求
     *
     * @param eventData 要发送的事件数据
     */
    public void sendEventRequest(EventDTO eventData) {
        String bashUri = "http://localhost:48888/slr-server/mock/backend";
        String uri;
        try {
            String stringBuilder = bashUri +
                    "?" + "channel=" + URLEncoder.encode(eventData.getChannel(), StandardCharsets.UTF_8.name()) +
                    "&" + "targetField=" + URLEncoder.encode(eventData.getTargetField(), StandardCharsets.UTF_8.name()) +
                    "&" + "targetValue=" + URLEncoder.encode(eventData.getTargetValue(), StandardCharsets.UTF_8.name()) +
                    "&" + "eventField=" + URLEncoder.encode(eventData.getEventField(), StandardCharsets.UTF_8.name()) +
                    "&" + "eventValue=" + URLEncoder.encode(eventData.getEventValue(), StandardCharsets.UTF_8.name()) +
                    "&" + "eventAttrMap=" + URLEncoder.encode(JsonUtils.toJsonString(eventData.getEventAttrMap()), StandardCharsets.UTF_8.name());
            uri = stringBuilder;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        // 发送 GET 请求，无需关注响应
        restTemplate.getForObject(uri, String.class);
    }
}
