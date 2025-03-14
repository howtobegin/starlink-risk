package com.liboshuai.slr.engine.biz.util;

import com.liboshuai.slr.engine.biz.constants.ParameterConstants;
import com.liboshuai.slr.framework.common.constants.DefaultConstants;
import com.liboshuai.slr.framework.common.constants.RedisKeyConstants;
import com.liboshuai.slr.framework.common.util.jasypt.JasyptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis读写工具类
 */
@Slf4j
public class RedisUtil {

    private static final JedisCluster jedisCluster;
    private static final String NAMESPACE;

    /**
     * 初始化连接池
     */
    static {
        // 节点信息
        String clusterNodes = ParameterUtil.getParameters().get(ParameterConstants.REDIS_CLUSTER_NODES);
        String[] nodes = clusterNodes.split(DefaultConstants.COMMA);
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        for (String node : nodes) {
            String[] hostPort = node.split(DefaultConstants.COLON);
            jedisClusterNodes.add(new HostAndPort(hostPort[0].trim(), Integer.parseInt(hostPort[1].trim())));
        }
        // 密码
        String decryptedPassword = ParameterUtil.getParameters().get(ParameterConstants.REDIS_PASSWORD);
        String password = JasyptUtil.decrypt(decryptedPassword);
        // 命名空间
        NAMESPACE = ParameterUtil.getParameters().get(ParameterConstants.REDIS_NAMESPACE, "starlink_risk") + RedisKeyConstants.REDIS_KEY_SPLIT;
        // 超时配置
        int connectionTimeout = Integer.parseInt(ParameterUtil.getParameters().get(ParameterConstants.REDIS_CONNECTION_TIMEOUT));
        int soTimeout = Integer.parseInt(ParameterUtil.getParameters().get(ParameterConstants.REDIS_SO_TIMEOUT));
        int maxAttempts = Integer.parseInt(ParameterUtil.getParameters().get(ParameterConstants.REDIS_MAX_ATTEMPTS));
        // 连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxWait(Duration.ofMillis(Long.parseLong(ParameterUtil.getParameters().get(ParameterConstants.REDIS_POOL_MAX_WAIT))));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(Long.parseLong(ParameterUtil.getParameters().get(ParameterConstants.REDIS_POOL_TIME_BETWEEN_EVICTION_RUNS))));
        poolConfig.setNumTestsPerEvictionRun(Integer.parseInt(ParameterUtil.getParameters().get(ParameterConstants.REDIS_POOL_NUM_TESTS_PER_EVICTION_RUN)));
        poolConfig.setMaxTotal(Integer.parseInt(ParameterUtil.getParameters().get(ParameterConstants.REDIS_POOL_MAX_TOTAL)));
        poolConfig.setMaxIdle(Integer.parseInt(ParameterUtil.getParameters().get(ParameterConstants.REDIS_POOL_MAX_IDLE)));
        poolConfig.setMinIdle(Integer.parseInt(ParameterUtil.getParameters().get(ParameterConstants.REDIS_POOL_MIN_IDLE)));
        poolConfig.setTestWhileIdle(true);
        // 创建JedisCluster
        if (!StringUtils.isEmpty(password)) {
            jedisCluster = new JedisCluster(jedisClusterNodes, connectionTimeout, soTimeout, maxAttempts, password, poolConfig);

        } else {
            jedisCluster = new JedisCluster(jedisClusterNodes, connectionTimeout, soTimeout, maxAttempts, poolConfig);
        }
        log.warn("RedisUtil 初始化完成，命名空间为: {}", NAMESPACE);
    }

    /**
     * Key 操作
     */
    public static boolean exists(String key) {
        return jedisCluster.exists(NAMESPACE + key);
    }

    public static void del(String key) {
        jedisCluster.del(NAMESPACE + key);
    }

    public static long ttl(String key) {
        return jedisCluster.ttl(NAMESPACE + key);
    }

    public static void expire(String key, long seconds) {
        jedisCluster.expire(NAMESPACE + key, seconds);
    }

    /**
     * String 操作
     */
    public static String getString(String key) {
        return jedisCluster.get(NAMESPACE + key);
    }

    public static void setString(String key, String value) {
        jedisCluster.set(NAMESPACE + key, value);
    }

    public static void setStringWithExpiry(String key, String value, long seconds) {
        jedisCluster.setex(NAMESPACE + key, seconds, value);
    }

    /**
     * List 操作
     */
    public static void lpush(String key, String... values) {
        jedisCluster.lpush(NAMESPACE + key, values);
    }

    public static List<String> lrange(String key, long start, long end) {
        return jedisCluster.lrange(NAMESPACE + key, start, end);
    }

    public static void lpushWithExpiry(String key, long seconds, String... values) {
        jedisCluster.lpush(NAMESPACE + key, values);
        jedisCluster.expire(NAMESPACE + key, seconds);
    }

    /**
     * Set 操作
     */
    public static void sadd(String key, String... members) {
        jedisCluster.sadd(NAMESPACE + key, members);
    }

    public static Set<String> smembers(String key) {
        return jedisCluster.smembers(NAMESPACE + key);
    }

    public static void saddWithExpiry(String key, long seconds, String... members) {
        jedisCluster.sadd(NAMESPACE + key, members);
        jedisCluster.expire(NAMESPACE + key, seconds);
    }

    /**
     * Hash 操作
     */
    public static void hset(String key, String field, String value) {
        jedisCluster.hset(NAMESPACE + key, field, value);
    }

    public static String hget(String key, String field) {
        return jedisCluster.hget(NAMESPACE + key, field);
    }

    public static Map<String, String> hgetAll(String key) {
        return jedisCluster.hgetAll(NAMESPACE + key);
    }

    public static void hsetWithExpiry(String key, long seconds, String field, String value) {
        jedisCluster.hset(NAMESPACE + key, field, value);
        jedisCluster.expire(NAMESPACE + key, seconds);
    }

    public static void hdel(final String key, final String... field) {
        jedisCluster.hdel(NAMESPACE + key, field);
    }

    /**
     * ZSet 操作
     */
    public static void zadd(String key, double score, String member) {
        jedisCluster.zadd(NAMESPACE + key, score, member);
    }

    public static Set<String> zrange(String key, long start, long end) {
        return jedisCluster.zrange(NAMESPACE + key, start, end);
    }

    public static void zaddWithExpiry(String key, long seconds, double score, String member) {
        jedisCluster.zadd(NAMESPACE + key, score, member);
        jedisCluster.expire(NAMESPACE + key, seconds);
    }

}
