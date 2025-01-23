package com.liboshuai.slr.module.engine.framework.connector;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

public class RedisSinkByBahirWithSet implements RedisMapper<Tuple2<String, String>> {

    /**
     * 指定Redis的命令为 SADD，用于向Set中添加元素
     */
    @Override
    public RedisCommandDescription getCommandDescription() {
        // 使用 SADD 命令
        return new RedisCommandDescription(RedisCommand.SADD);
    }

    /**
     * 从数据流里获取Key（Set名称）
     */
    @Override
    public String getKeyFromData(Tuple2<String, String> tuple2) {
        // 来指定 Set 的名称，可以是固定值或从输入数据动态决定
        return tuple2.f0;
    }

    /**
     * 从数据流里获取Value（Set的成员值）
     */
    @Override
    public String getValueFromData(Tuple2<String, String> tuple2) {
        // 指定需要加入到 Set 的成员值
        return tuple2.f1;
    }
}