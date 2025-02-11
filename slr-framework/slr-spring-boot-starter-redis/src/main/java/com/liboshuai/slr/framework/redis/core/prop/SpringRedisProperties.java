package com.liboshuai.slr.framework.redis.core.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liboshuai
 * @version 1.0
 */
@Data
@ConfigurationProperties(prefix = "spring.redis")
public class SpringRedisProperties {

    private String namespace = "starlink_risk";

}

