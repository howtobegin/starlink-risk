package com.liboshuai.slr.module.admin.service.mock;

public interface MockService {

    /**
     * 创建事件数据文件（文件内容为单条上送模式）
     *
     * @param startMillis    开始的时间戳
     * @param durationMillis 时间跨度
     * @param perSecondCount 每秒生成的时间戳数量
     */
    void createEventFileSingleMode(long startMillis, long durationMillis, int perSecondCount);

    /**
     * 创建事件数据文件（文件内容为批量上送模式）
     *
     * @param startMillis    开始的时间戳
     * @param durationMillis 时间跨度
     * @param perSecondCount 每秒生成的时间戳数量
     */
    void createEventFileBatchMode(long startMillis, long durationMillis, int perSecondCount);
}
