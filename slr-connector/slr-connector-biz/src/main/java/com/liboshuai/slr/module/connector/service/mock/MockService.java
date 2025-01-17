package com.liboshuai.slr.module.connector.service.mock;

public interface MockService {

    /**
     * 创建事件数据文件（文件内容为批量上送模式）
     */
    void createEventFileBatchMode(Long totalCount, Long maxEntries);
}
