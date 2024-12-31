package com.liboshuai.slr.framework.redis.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.liboshuai.slr.framework.common.executor.SlrThreadExecutor;
import com.liboshuai.slr.framework.redis.core.listener.AbstractChannelMessageListener;
import com.liboshuai.slr.framework.redis.core.listener.CaffeineCacheRemovalListener;
import com.liboshuai.slr.framework.redis.core.listener.RedisCacheMessageListener;
import com.liboshuai.slr.framework.redis.core.manager.CustomRedisCache;
import com.liboshuai.slr.framework.redis.core.manager.MultilevelCache;
import com.liboshuai.slr.framework.redis.core.prop.MultilevelCacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;

/**
 * @author liboshuai
 * @version 1.0
 */
@Slf4j
@EnableCaching
@AutoConfiguration
@EnableConfigurationProperties({MultilevelCacheProperties.class, CacheProperties.class})
public class MultilevelCacheAutoConfiguration {

    ExecutorService cacheExecutor = new SlrThreadExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 20,
            Runtime.getRuntime().availableProcessors() * 200,
            "cache-pool"
    );
    @Resource
    private MultilevelCacheProperties multilevelCacheProperties;

    @Bean
    public RedisCache redisCache(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        RedisCacheConfiguration redisCacheConfiguration = defaultCacheConfig();
        redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.of(multilevelCacheProperties.getRedisExpireTime(), ChronoUnit.SECONDS));
        redisCacheConfiguration = redisCacheConfiguration.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return new CustomRedisCache(multilevelCacheProperties.getRedisName(), redisCacheWriter, redisCacheConfiguration);
    }

    /**
     * 由于Caffeine 不会再值过期后立即执行清除，而是在写入或者读取操作之后执行少量维护工作，或者在写入读取很少的情况下，偶尔执行清除操作。
     * 如果我们项目写入或者读取频率很高，那么不用担心。如果想入写入和读取操作频率较低，那么我们可以通过Cache.cleanUp()或者加scheduler去定时执行清除操作。
     * Scheduler可以迅速删除过期的元素，***Java 9 +***后的版本，可以通过Scheduler.systemScheduler(), 调用系统线程，达到定期清除的目的
     */
    @Bean
    @ConditionalOnClass(CaffeineCache.class)
    @ConditionalOnProperty(name = "multilevel.cache.caffeineSwitch", havingValue = "true", matchIfMissing = true)
    public CaffeineCache caffeineCache() {
        return new CaffeineCache(multilevelCacheProperties.getCaffeineName(), Caffeine.newBuilder()
                // 设置初始缓存大小
                .initialCapacity(multilevelCacheProperties.getInitCapacity())
                // 设置最大缓存
                .maximumSize(multilevelCacheProperties.getMaxCapacity())
                // 设置缓存线程池
                .executor(cacheExecutor)
                // 设置定时任务执行过期清除操作
//                .scheduler(Scheduler.systemScheduler())
                // 监听器(超出最大缓存)
                .removalListener(new CaffeineCacheRemovalListener())
                // 设置缓存读时间的过期时间
                .expireAfterAccess(Duration.of(multilevelCacheProperties.getCaffeineExpireTime(), ChronoUnit.SECONDS))
                // 开启metrics监控
                .recordStats()
                .build());
    }

    @Bean
    @ConditionalOnBean({CaffeineCache.class, RedisCache.class})
    public MultilevelCache multilevelCache(RedisCache redisCache, CaffeineCache caffeineCache) {
        return new MultilevelCache(true, redisCache, caffeineCache);
    }

    @Bean
    public RedisCacheMessageListener redisCacheMessageListener(@Autowired CaffeineCache caffeineCache) {
        RedisCacheMessageListener redisCacheMessageListener = new RedisCacheMessageListener();
        redisCacheMessageListener.setCaffeineCache(caffeineCache);
        return redisCacheMessageListener;
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // config = config.entryTtl();
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        //将配置文件中所有的配置都生效
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }

    /**
     * 创建 Redis Pub/Sub 广播消费的容器
     * 统一在这里注入监听器容器，解决多处注入导致冲突问题
     * 我们只需要在业务侧注入监听器bean即可，这里要求监听器bean extends {@link AbstractChannelMessageListener}
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                       List<AbstractChannelMessageListener<?>> listeners) {
        // 创建 RedisMessageListenerContainer 对象
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        // 设置 RedisConnection 工厂。
        container.setConnectionFactory(redisConnectionFactory);
        // 添加监听器
        listeners.forEach(listener -> {
            container.addMessageListener(listener, new ChannelTopic(listener.getChannel()));
            log.info("[redisMessageListenerContainer][注册 Channel({}) 对应的监听器({})]",
                    listener.getChannel(), listener.getClass().getName());
        });
        return container;
    }
}
