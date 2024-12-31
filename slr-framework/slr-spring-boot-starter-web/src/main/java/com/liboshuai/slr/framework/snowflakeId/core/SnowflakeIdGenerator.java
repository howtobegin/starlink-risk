package com.liboshuai.slr.framework.snowflakeId.core;


import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 基于 Hutool 封装的雪花算法 ID 生成器
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

    /**
     * 生成下一个 ID
     *
     * @return long 类型的 ID
     */
    public long nextId() {
        return snowflake.nextId();
    }

    /**
     * 生成下一个 ID 的字符串表示
     *
     * @return String 类型的 ID
     */
    public String nextIdStr() {
        return snowflake.nextIdStr();
    }
}
