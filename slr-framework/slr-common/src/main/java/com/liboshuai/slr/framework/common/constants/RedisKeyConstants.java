package com.liboshuai.slr.framework.common.constants;

/**
 * redis的key常量
 */
public interface RedisKeyConstants {
    // ****************************************** 全局 ******************************************

    /**
     * redis key 分隔符
     */
    String REDIS_KEY_SPLIT = "::";

    // ****************************************** 缓存 ******************************************

    /**
     * 规则信息
     */
    String RULE_INFO = "ruleInfo";

    // ****************************************** 业务 ******************************************

    /**
     * doris事件历史值
     */
    String DORIS_EVENT_HISTORY_VALUE = "dorisEventHistoryValue";
}
