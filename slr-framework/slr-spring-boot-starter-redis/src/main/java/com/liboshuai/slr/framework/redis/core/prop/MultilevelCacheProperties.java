package com.liboshuai.slr.framework.redis.core.prop;

import com.liboshuai.slr.framework.redis.core.listener.CacheMessage;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liboshuai
 * @version 1.0
 */
@Data
@ConfigurationProperties(prefix = "multilevel.cache")
public class MultilevelCacheProperties {

    /**
     * 一级本地缓存最大容量
     */
    private Integer maxCapacity = 512;

    /**
     * 一级本地缓存初始化容量
     */
    private Integer initCapacity = 64;

    /**
     * 消息主题
     * 在{@link CacheMessage#getChannel()}中设置
     */
//    private String topic = "multilevel-cache-topic";

    /**
     * 缓存名称
     */
    private String name = "multilevel-cache";

    /**
     * 一级本地缓存名称
     */
    private String caffeineName = "multilevel-caffeine-cache";

    /**
     * 二级缓存名称
     */
    private String redisName = "starlink_risk::multilevel-redis-cache";

    /**
     * 一级本地缓存过期时间
     */
    private Integer caffeineExpireTime = 300;

    /**
     * 二级缓存过期时间
     */
    private Integer redisExpireTime = 600;

    /**
     * 一级缓存开关
     */
    private Boolean caffeineSwitch = true;

}

