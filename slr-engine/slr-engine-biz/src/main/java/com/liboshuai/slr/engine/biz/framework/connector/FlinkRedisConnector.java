package com.liboshuai.slr.engine.biz.framework.connector;

import com.liboshuai.slr.engine.biz.constants.ParameterConstants;
import com.liboshuai.slr.engine.biz.util.ParameterUtil;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisClusterConfig;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class FlinkRedisConnector {
    private static final FlinkJedisClusterConfig JEDIS_CONF;

    static {
        ParameterTool parameterTool = ParameterUtil.getParameters();
        // 获取 Redis 集群节点信息
        String redisClusterNodes = parameterTool.get(ParameterConstants.REDIS_CLUSTER_NODES);
        // 获取 Redis 密码
        String password = parameterTool.get(ParameterConstants.REDIS_PASSWORD);
        // 设置超时时间
        int timeout = parameterTool.getInt(ParameterConstants.REDIS_CONNECTION_TIMEOUT);
        // 最小空闲连接数
        int minIdle = parameterTool.getInt(ParameterConstants.REDIS_POOL_MIN_IDLE);
        // 最大空闲连接数
        int maxIdle = parameterTool.getInt(ParameterConstants.REDIS_POOL_MAX_IDLE);
        // 最大连接数
        int maxTotal = parameterTool.getInt(ParameterConstants.REDIS_POOL_MAX_TOTAL);
        // 最大尝试次数
        int maxAttempts = parameterTool.getInt(ParameterConstants.REDIS_MAX_ATTEMPTS);


        // 将 Redis 节点信息转换为 Set<InetSocketAddress>
        Set<InetSocketAddress> inetSocketAddressSet = new HashSet<>();
        if (redisClusterNodes != null && !redisClusterNodes.isEmpty()) {
            String[] nodes = redisClusterNodes.split(DefaultConstants.COMMA); // 假定节点是逗号分隔的（例如 one:6379,two:6380）
            for (String node : nodes) {
                String[] hostAndPort = node.split(DefaultConstants.COLON); // 每个节点以冒号分隔，如 one:6379
                if (hostAndPort.length == 2) {
                    String host = hostAndPort[0];
                    int port = Integer.parseInt(hostAndPort[1]);
                    inetSocketAddressSet.add(new InetSocketAddress(host, port)); // 添加到地址集合
                }
            }
        }

        // 初始化 FlinkJedisClusterConfig 配置
        JEDIS_CONF = new FlinkJedisClusterConfig
                .Builder()
                .setNodes(inetSocketAddressSet) // 设置 Redis 节点信息
                .setPassword(password) // 设置 Redis 密码
                .setTimeout(timeout) // 设置超时时间（以毫秒为单位）
                .setMinIdle(minIdle) // 最小空闲连接数
                .setMaxIdle(maxIdle) // 最大空闲连接数
                .setMaxTotal(maxTotal) // 最大连接数，适用于高并发
                .setMaxRedirections(maxAttempts) // 最大重定向次数，用于集群配置
                .setTestOnBorrow(true) // 连接获取时是否验证连接有效性
                .setTestOnReturn(false) // 连接归还时是否验证有效性
                .setTestWhileIdle(true) // 空闲时是否验证连接有效性
                .build();
    }

    /**
     * 基于 Bahir 写入 Redis，Redis 的数据是 set 类型
     */
    public static void writeByBahirWithSet(DataStream<Tuple2<String, String>> input) {
        input.addSink(new RedisSink<>(JEDIS_CONF, new RedisSinkByBahirWithSet()));
    }
}