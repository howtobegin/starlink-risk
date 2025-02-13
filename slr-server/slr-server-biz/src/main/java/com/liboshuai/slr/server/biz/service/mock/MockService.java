package com.liboshuai.slr.server.biz.service.mock;

import com.liboshuai.slr.engine.api.dto.EventDTO;

import java.util.List;

public interface MockService {

    /**
     * 创建事件数据文件（文件内容为批量上送模式）
     */
    void createEventFile(Long totalCount, Long maxEntries);

    void pushEventToNginx(List<EventDTO> eventDTOList);
}
