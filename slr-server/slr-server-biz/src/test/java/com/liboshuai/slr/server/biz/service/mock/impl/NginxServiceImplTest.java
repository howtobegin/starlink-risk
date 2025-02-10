package com.liboshuai.slr.server.biz.service.mock.impl;

import com.liboshuai.slr.engine.api.dto.NginxEventDTO;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
class NginxServiceImplTest {

    @Test
    void testNginxBackendRequest() throws UnsupportedEncodingException {
        String json = "{\"eventTime\": \"1739178682.366\",\"channel\": \"game\",\"targetField\": \"userId\",\"targetValue\": \"U000000002\",\"eventField\": \"lottery\",\"eventValue\": \"1\",\"eventAttrMap\": \"%257B%2522campaignId%2522%253A%2522C000000004%2522%252C%2522bankNo%2522%253A%25226100%2522%252C%2522bankName%2522%253A%2522%25E9%2582%25AE%25E5%2582%25A8%25E9%2593%25B6%25E8%25A1%258C%2522%252C%2522campaignName%2522%253A%2522%25E6%25B4%25BB%25E5%258A%25A84%2522%257D\"}";
        NginxEventDTO nginxEventDTO = JsonUtils.parseObject(json, NginxEventDTO.class);
        if (Objects.isNull(nginxEventDTO)) {
            throw new RuntimeException("nginxEventDTO must not be null");
        }
        nginxEventDTO.setEventTime(URLDecoder.decode(nginxEventDTO.getEventTime(), StandardCharsets.UTF_8.name()));
        nginxEventDTO.setChannel(URLDecoder.decode(nginxEventDTO.getChannel(), StandardCharsets.UTF_8.name()));
        nginxEventDTO.setTargetField(URLDecoder.decode(nginxEventDTO.getTargetField(), StandardCharsets.UTF_8.name()));
        nginxEventDTO.setTargetValue(URLDecoder.decode(nginxEventDTO.getTargetValue(), StandardCharsets.UTF_8.name()));
        nginxEventDTO.setEventField(URLDecoder.decode(nginxEventDTO.getEventField(), StandardCharsets.UTF_8.name()));
        nginxEventDTO.setEventValue(URLDecoder.decode(nginxEventDTO.getEventValue(), StandardCharsets.UTF_8.name()));
        nginxEventDTO.setEventAttrMap(URLDecoder.decode(URLDecoder.decode(nginxEventDTO.getEventAttrMap(), StandardCharsets.UTF_8.name()), StandardCharsets.UTF_8.name()));
        log.info("nginxEventDTO: {}", nginxEventDTO);
    }
}