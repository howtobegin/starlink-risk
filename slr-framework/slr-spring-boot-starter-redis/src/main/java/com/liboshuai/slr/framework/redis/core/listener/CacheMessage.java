package com.liboshuai.slr.framework.redis.core.listener;

import com.liboshuai.slr.framework.redis.core.message.AbstractChannelMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author liboshuai
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CacheMessage extends AbstractChannelMessage implements Serializable {
    private String cacheName;
    private Object key;
    private Object value;
    private Integer type;

    @Override
    public String getChannel() {
        return "multilevel-cache-topic";
    }
}
