package com.liboshuai.slr.framework.idempotent.config;

import com.liboshuai.slr.framework.idempotent.core.aop.IdempotentAspect;
import com.liboshuai.slr.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import com.liboshuai.slr.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import com.liboshuai.slr.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import com.liboshuai.slr.framework.idempotent.core.redis.IdempotentRedisDAO;
import com.liboshuai.slr.framework.redis.config.SlrRedisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration(after = SlrRedisAutoConfiguration.class)
public class SlrIdempotentConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentRedisDAO(stringRedisTemplate);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
