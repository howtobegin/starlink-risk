package com.liboshuai.slr.server.biz.service.mock.impl;

import com.liboshuai.slr.engine.api.dto.EventDTO;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.server.biz.service.mock.NginxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

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
        // 打点服务器的请求 URI 地址（建议动态配置）
        String baseUri = "http://localhost:48881/backend.gif";

        // 使用 UriComponentsBuilder 构建 URI，并添加查询参数
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(baseUri)
                .queryParam("channel", eventData.getChannel())
                .queryParam("targetField", eventData.getTargetField())
                .queryParam("targetValue", eventData.getTargetValue())
                .queryParam("eventField", eventData.getEventField())
                .queryParam("eventValue", eventData.getEventValue())
                .queryParam("eventAttrMap", JsonUtils.toJsonString(eventData.getEventAttrMap()))
                .build()
                .encode(StandardCharsets.UTF_8); // 让 UriComponentsBuilder 进行 UTF-8 编码

        // 向打点服务器发送数据（无需关注响应）
        restTemplate.getForObject(uriComponents.toUri(), String.class);
    }
}
