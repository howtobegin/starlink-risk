package com.liboshuai.slr.server.biz.service.mock;

import com.liboshuai.slr.engine.api.dto.MockEventDTO;

public interface NginxService {
    void testNginxBackendRequest();

    void sendEventRequest(MockEventDTO mockEventDTO);
}
