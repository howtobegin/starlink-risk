package com.liboshuai.slr.module.admin.framework.component.snowflake;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 根据hutool封装的雪花算法ID生成器
 */
@Component
public class SnowflakeIdGenerator {


    private final cn.hutool.core.lang.Snowflake snowflake;

    @Autowired
    public SnowflakeIdGenerator(SnowflakeIdProperties properties) {
        long workerIdLong = Long.parseLong(properties.getWorkerId());
        long datacenterIdLong = Long.parseLong(properties.getDatacenterId());
        snowflake = IdUtil.getSnowflake(workerIdLong, datacenterIdLong);
    }

    public long nextId() {
        return snowflake.nextId();
    }

    public String nextIdStr() {
        return snowflake.nextIdStr();
    }
}
