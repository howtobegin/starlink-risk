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
        String json = "{\"eventTime\": \"1739192209.910\",\"channel\": \"game\",\"targetField\": \"userId\",\"targetValue\": \"U000000002\",\"eventField\": \"lottery\",\"eventValue\": \"1\",\"eventAttrMap\": \"%7B%22campaignId%22:%22C000000004%22,%22bankNo%22:%226100%22,%22bankName%22:%22%E9%82%AE%E5%82%A8%E9%93%B6%E8%A1%8C%22,%22campaignName%22:%22%E6%B4%BB%E5%8A%A84%22%7D\"}";
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