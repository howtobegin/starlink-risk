package com.liboshuai.slr.server.biz.service.mock.impl;

import com.liboshuai.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.slr.server.biz.service.mock.NginxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
        KafkaEventDTO eventData = createEventData();
        // 向打点服务器发送请求
        sendEventRequest(eventData);
    }

    /**
     * 模拟业务系统产生事件数据
     *
     * @return 构建好的 KafkaEventDTO 对象
     */
    public KafkaEventDTO createEventData() {
        Map<String, String> eventAttributes = new HashMap<>();
        eventAttributes.put("campaignId", "C000000004");
        eventAttributes.put("campaignName", "活动4");
        eventAttributes.put("bankName", "邮储银行");
        eventAttributes.put("bankNo", "6100");

        return KafkaEventDTO.builder()
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
    public void sendEventRequest(KafkaEventDTO eventData) {
        String uri;
        try {
            uri = UriComponentsBuilder.fromHttpUrl("http://localhost:48881/backend.gif")
                    .queryParam("channel", URLEncoder.encode(eventData.getChannel(), "UTF-8"))
                    .queryParam("targetField", URLEncoder.encode(eventData.getTargetField(), "UTF-8"))
                    .queryParam("targetValue", URLEncoder.encode(eventData.getTargetValue(), "UTF-8"))
                    .queryParam("eventField", URLEncoder.encode(eventData.getEventField(), "UTF-8"))
                    .queryParam("eventValue", URLEncoder.encode(eventData.getEventValue(), "UTF-8"))
                    .queryParam("eventAttrMap", URLEncoder.encode(eventData.getEventAttrMap().toString(), "UTF-8"))
                    .toUriString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL 编码失败", e);
        }

        // 发送 GET 请求，无需关注响应
        restTemplate.getForObject(uri, String.class);
    }
}
