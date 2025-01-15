package com.liboshuai.slr.framework.common.constants;

/**
 * redis的key常量
 */
public interface RedisKeyConstants {
    // ****************************************** 全局 ******************************************

    /**
     * redis key 统一前缀
     */
    String REDIS_KEY_PREFIX = "starlink_risk";

    /**
     * redis key 分隔符
     */
    String REDIS_KEY_SPLIT = "::";


    // ****************************************** 业务 ******************************************

    /**
     * doris事件历史值
     */
    String DORIS_EVENT_HISTORY_VALUE = "dorisEventHistoryValue";
}
