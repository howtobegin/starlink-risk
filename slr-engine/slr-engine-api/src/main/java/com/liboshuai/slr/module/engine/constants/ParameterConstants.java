package com.liboshuai.slr.module.engine.constants;

/**
 * author: liboshuai
 * description: 配置参数名称工具类
 * date: 2023
 */

public interface ParameterConstants {


    /* **********************
     *
     * Flink 配置参数名称
     *
     * *********************/

    //触发checkpoint时间间隔
    String FLINK_CHECKPOINT_INTERVAL = "flink.checkpoint.interval";
    //checkpoint超时
    String FLINK_CHECKPOINT_TIMEOUT = "flink.checkpoint.timeout";
    //checkpoint允许失败次数
    String FLINK_CHECKPOINT_FAILURENUMBER = "flink.checkpoint.failureNumber";
    //同一时间checkpoint数量
    String FLINK_CHECKPOINT_MAXCONCURRENT = "flink.checkpoint.maxConcurrent";
    // flink checkpoint 存储的 hdfs 用户名
    String FLINK_CHECKPOINT_HDFS_USERNAME = "flink.checkpoint.hdfs.username";
    // flink checkpoint 存储的路径
    String FLINK_CHECKPOINT_URL = "flink.checkpoint.url";
    //并行度
    String FLINK_PARALLELISM = "flink.parallelism";
    //数据延迟的最大时间
    String FLINK_MAXOUTOFORDERNESS = "flink.maxOutOfOrderness";

    /* **********************
     *
     * Kafka 配置参数名称
     *
     * *********************/

    String KAFKA_SOURCE_BROKERS = "kafka.source.brokers";
    String KAFKA_SOURCE_TOPIC = "kafka.source.topic";
    String KAFKA_SOURCE_GROUP = "kafka.source.group";
    String KAFKA_SINK_BROKERS = "kafka.sink.brokers";
    String KAFKA_SINK_TOPIC_ALERT = "kafka.sink.topic.alert";

    /**
     * Doris 配置参数名称
     */

    String DORIS_FE_HOST = "doris.fe.host";
    String DORIS_FE_PORT_HTTP = "doris.fe.port.http";
    String DORIS_FE_PORT_QUERY = "doris.fe.port.query";
    String DORIS_USERNAME = "doris.username";
    String DORIS_PASSWORD = "doris.password";
    String DORIS_DATABASE = "doris.database";
    String DORIS_TABLE_EVENT = "doris.table.event";
    String DORIS_TABLE_KEY = "doris.table.key";


    /* **********************
     *
     * Mysql 配置参数名称
     *
     * *********************/

    String MYSQL_HOSTNAME = "mysql.hostname";
    String MYSQL_PORT = "mysql.port";
    String MYSQL_USERNAME = "mysql.username";
    String MYSQL_PASSWORD = "mysql.password";
    String MYSQL_DATABASE = "mysql.database";
    String MYSQL_TABLE_RULEJSON = "mysql.table.ruleJson";

    /* **********************
     *
     * Redis 配置参数名称
     *
     * *********************/

    String REDIS_CLUSTER_NODES = "redis.cluster.nodes";
    String REDIS_PASSWORD = "redis.password";
    String REDIS_CONNECTION_TIMEOUT = "redis.connectionTimeout";
    String REDIS_SO_TIMEOUT = "redis.soTimeout";
    String REDIS_MAX_ATTEMPTS = "redis.maxAttempts";
    String REDIS_POOL_MAX_WAIT = "redis.pool.maxWait";
    String REDIS_POOL_TIME_BETWEEN_EVICTION_RUNS = "redis.pool.timeBetweenEvictionRuns";
    String REDIS_POOL_NUM_TESTS_PER_EVICTION_RUN = "redis.pool.numTestsPerEvictionRun";
    String REDIS_POOL_MAX_TOTAL = "redis.pool.maxTotal";
    String REDIS_POOL_MAX_IDLE = "redis.pool.maxIdle";
    String REDIS_POOL_MIN_IDLE = "redis.pool.minIdle";


    /* **********************
     *
     * Flink环境 配置参数名称
     *
     * *********************/

    //当前环境
    String FLINK_ENV_ACTIVE = "flink.env.active";
    /* **********************
     *
     * Flink 配置文件
     *
     * *********************/
    //根配置文件
    String FLINK_ROOT_FILE = "flink.properties";
    //不同环境配置文件
    String FLINK_ENV_FILE = "flink-%s.properties";
}
