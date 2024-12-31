package com.liboshuai.slr.framework.snowflakeId.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "snowflake")
public class SnowflakeIdProperties {

    /**
     * 工作节点ID (0-31)
     */
    private String workerId;

    /**
     * 数据中心ID (0-31)
     */
    private String datacenterId;
}