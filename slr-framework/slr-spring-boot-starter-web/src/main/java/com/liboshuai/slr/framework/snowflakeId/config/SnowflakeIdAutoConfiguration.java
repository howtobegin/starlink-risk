package com.liboshuai.slr.framework.snowflakeId.config;

import com.liboshuai.slr.framework.snowflakeId.core.SnowflakeIdGenerator;
import com.liboshuai.slr.framework.snowflakeId.core.SnowflakeIdProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 自动配置 SnowflakeIdGenerator
 */
@AutoConfiguration
@ConditionalOnClass(SnowflakeIdGenerator.class)
@EnableConfigurationProperties(SnowflakeIdProperties.class)
public class SnowflakeIdAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeIdGenerator snowflakeIdGenerator(SnowflakeIdProperties properties) {
        return new SnowflakeIdGenerator(properties);
    }
}