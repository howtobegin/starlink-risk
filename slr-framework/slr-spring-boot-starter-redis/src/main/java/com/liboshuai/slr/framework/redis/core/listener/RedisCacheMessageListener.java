package com.liboshuai.slr.framework.redis.core.listener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Objects;

/**
 * @author liboshuai
 * @version 1.0
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class RedisCacheMessageListener extends AbstractChannelMessageListener<CacheMessage> {

    private CaffeineCache caffeineCache;

    @Override
    public void onMessage(CacheMessage message) {
        log.info("监听的redis message: {}", message);
        if (Objects.isNull(message.getKey())) {
            caffeineCache.invalidate();
        } else {
            caffeineCache.evict(message.getKey());
        }
    }
}
