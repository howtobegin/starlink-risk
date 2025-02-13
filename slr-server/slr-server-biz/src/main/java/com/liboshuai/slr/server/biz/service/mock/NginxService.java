package com.liboshuai.slr.server.biz.service.mock;

import com.liboshuai.slr.engine.api.dto.EventDTO;

public interface NginxService {
    void testNginxBackendRequest();

    void sendEventRequest(EventDTO eventDTO);
}
