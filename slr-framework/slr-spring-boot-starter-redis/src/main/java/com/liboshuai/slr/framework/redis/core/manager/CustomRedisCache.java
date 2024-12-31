package com.liboshuai.slr.framework.redis.core.manager;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * @author liboshuai
 * @version 1.0
 */
public class CustomRedisCache extends RedisCache {

    /**
     * redisCache的构造方法是protected，外部不能调用，所以通过该类来new redisCache
     */
    public CustomRedisCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig) {
        super(name, cacheWriter, cacheConfig);
    }
}