package com.liboshuai.slr.framework.redis.core.message;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author liboshuai
 * @version 1.0
 */
public abstract class AbstractChannelMessage {

    /**
     * 获得 Redis Channel
     *
     * @return Channel
     */
    @JsonIgnore // 避免序列化。原因是，Redis 发布 Channel 消息的时候，已经会指定。
    public abstract String getChannel();

}