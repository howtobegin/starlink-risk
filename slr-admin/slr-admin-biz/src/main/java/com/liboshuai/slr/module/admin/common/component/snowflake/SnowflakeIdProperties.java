package com.liboshuai.slr.module.admin.common.component.snowflake;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "snowflake")
public class SnowflakeIdProperties {

    private String workerId;
    private String datacenterId;
}