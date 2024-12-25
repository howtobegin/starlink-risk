package com.liboshuai.slr.framework.ratelimiter.config;

import com.liboshuai.slr.framework.ratelimiter.core.aop.RateLimiterAspect;
import com.liboshuai.slr.framework.ratelimiter.core.keyresolver.RateLimiterKeyResolver;
import com.liboshuai.slr.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import com.liboshuai.slr.framework.ratelimiter.core.keyresolver.impl.DefaultRateLimiterKeyResolver;
import com.liboshuai.slr.framework.ratelimiter.core.keyresolver.impl.ExpressionRateLimiterKeyResolver;
import com.liboshuai.slr.framework.ratelimiter.core.keyresolver.impl.ServerNodeRateLimiterKeyResolver;
import com.liboshuai.slr.framework.ratelimiter.core.redis.RateLimiterRedisDAO;
import com.liboshuai.slr.framework.redis.config.SlrRedisAutoConfiguration;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration(after = SlrRedisAutoConfiguration.class)
public class SlrRateLimiterConfiguration {

    @Bean
    public RateLimiterAspect rateLimiterAspect(List<RateLimiterKeyResolver> keyResolvers, RateLimiterRedisDAO rateLimiterRedisDAO) {
        return new RateLimiterAspect(keyResolvers, rateLimiterRedisDAO);
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RateLimiterRedisDAO rateLimiterRedisDAO(RedissonClient redissonClient) {
        return new RateLimiterRedisDAO(redissonClient);
    }

    // ========== 各种 RateLimiterRedisDAO Bean ==========

    @Bean
    public DefaultRateLimiterKeyResolver defaultRateLimiterKeyResolver() {
        return new DefaultRateLimiterKeyResolver();
    }

    @Bean
    public ClientIpRateLimiterKeyResolver clientIpRateLimiterKeyResolver() {
        return new ClientIpRateLimiterKeyResolver();
    }

    @Bean
    public ServerNodeRateLimiterKeyResolver serverNodeRateLimiterKeyResolver() {
        return new ServerNodeRateLimiterKeyResolver();
    }

    @Bean
    public ExpressionRateLimiterKeyResolver expressionRateLimiterKeyResolver() {
        return new ExpressionRateLimiterKeyResolver();
    }

}
