package com.liboshuai.starlink.slr.engine.common;

/**
 * author: liboshuai
 * description: 配置参数名称工具类
 * date: 2023
 */

public class ParameterConstants {


    /* **********************
     *
     * Flink 配置参数名称
     *
     * *********************/

    // flink 本地模式运行临时目录
    public static final String FLINK_TMPDIR = "flink.tmpdir";
    //触发checkpoint时间间隔
    public static final String FLINK_CHECKPOINT_INTERVAL = "flink.checkpoint.interval";
    //checkpoint超时
    public static final String FLINK_CHECKPOINT_TIMEOUT = "flink.checkpoint.timeout";
    //checkpoint允许失败次数
    public static final String FLINK_CHECKPOINT_FAILURENUMBER = "flink.checkpoint.failureNumber";
    //同一时间checkpoint数量
    public static final String FLINK_CHECKPOINT_MAXCONCURRENT = "flink.checkpoint.maxConcurrent";
    // flink checkpoint 存储的 hdfs 用户名
    public static final String FLINK_CHECKPOINT_HDFS_USERNAME = "flink.checkpoint.hdfs.username";
    // flink checkpoint 存储的路径
    public static final String FLINK_CHECKPOINT_URL = "flink.checkpoint.url";
    //并行度
    public static final String FLINK_PARALLELISM = "flink.parallelism";
    //数据延迟的最大时间
    public static final String FLINK_MAXOUTOFORDERNESS = "flink.maxOutOfOrderness";

    /* **********************
     *
     * Kafka 配置参数名称
     *
     * *********************/

    public static final String KAFKA_SOURCE_BROKERS = "kafka.source.brokers";
    public static final String KAFKA_SOURCE_TOPIC = "kafka.source.topic";
    public static final String KAFKA_SOURCE_GROUP = "kafka.source.group";
    public static final String KAFKA_SINK_BROKERS = "kafka.sink.brokers";
    public static final String KAFKA_SINK_TOPIC = "kafka.sink.topic";


    /* **********************
     *
     * Mysql 配置参数名称
     *
     * *********************/

    public static final String MYSQL_HOSTNAME = "mysql.hostname";
    public static final String MYSQL_PORT = "mysql.port";
    public static final String MYSQL_USERNAME = "mysql.username";
    public static final String MYSQL_PASSWORD = "mysql.password";
    public static final String MYSQL_DATABASE = "mysql.database";
    public static final String MYSQL_TABLE_RULE_JSON = "mysql.table.ruleJson";
    public static final String MYSQL_TABLE_RULE_COUNT = "mysql.table.ruleCount";
    public static final String MYSQL_TABLE_BANK = "mysql.table.bank";

    /* **********************
     *
     * Redis 配置参数名称
     *
     * *********************/

    public static final String REDIS_CLUSTER_NODES = "redis.cluster.nodes";
    public static final String REDIS_PASSWORD = "redis.password";
    public static final String REDIS_CONNECTION_TIMEOUT = "redis.connectionTimeout";
    public static final String REDIS_SO_TIMEOUT = "redis.soTimeout";
    public static final String REDIS_MAX_ATTEMPTS = "redis.maxAttempts";
    public static final String REDIS_POOL_MAX_WAIT = "redis.pool.maxWait";
    public static final String REDIS_POOL_TIME_BETWEEN_EVICTION_RUNS = "redis.pool.timeBetweenEvictionRuns";
    public static final String REDIS_POOL_NUM_TESTS_PER_EVICTION_RUN = "redis.pool.numTestsPerEvictionRun";
    public static final String REDIS_POOL_MAX_TOTAL = "redis.pool.maxTotal";
    public static final String REDIS_POOL_MAX_IDLE = "redis.pool.maxIdle";
    public static final String REDIS_POOL_MIN_IDLE = "redis.pool.minIdle";


    /* **********************
     *
     * Flink环境 配置参数名称
     *
     * *********************/

    //当前环境
    public static final String FLINK_ENV_ACTIVE = "flink.env.active";
    /* **********************
     *
     * Flink 配置文件
     *
     * *********************/
    //根配置文件
    public static final String FLINK_ROOT_FILE = "flink.properties";
    //不同环境配置文件
    public static final String FLINK_ENV_FILE = "flink-%s.properties";

}
