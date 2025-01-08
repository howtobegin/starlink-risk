# 进入 fe 容器内

```
docker-compose exec fe bash

mysql -h ${宿主机IP} -P 9030 -uroot

SET PASSWORD FOR 'root' = PASSWORD('Rongshu@2024');
use mysql;
ALTER SYSTEM ADD BACKEND "${宿主机IP}:9050";
SHOW PROC '/backends';
CREATE DATABASE IF NOT EXISTS `starlink_risk`;
use starlink_risk;

drop table if exists slr_event;
CREATE TABLE IF NOT EXISTS slr_event
(
    `EVENT_TIME`    DATETIME    NOT NULL COMMENT '事件时间',
    `CHANNEL`       VARCHAR(16) NOT NULL COMMENT '渠道',
    `TARGET_FIELD`  VARCHAR(64) NOT NULL COMMENT '目标编号',
    `TARGET_VALUE`  VARCHAR(64) NOT NULL COMMENT '目标值',
    `EVENT_FIELD`   VARCHAR(64) NOT NULL COMMENT '事件编号',
    `EVENT_VALUE`   VARCHAR(64) NOT NULL COMMENT '事件值',
    `EVENT_ATTR_MAP` MAP< STRING,STRING> NULL COMMENT '事件属性'
)
    ENGINE = OLAP
    DUPLICATE KEY(`EVENT_TIME`, `CHANNEL`, `TARGET_FIELD`, `TARGET_VALUE`)
PARTITION BY RANGE(`EVENT_TIME`) ()
DISTRIBUTED BY HASH(`EVENT_TIME`, `CHANNEL`, `TARGET_FIELD`, `TARGET_VALUE`) BUCKETS AUTO
PROPERTIES
(
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH",
    "dynamic_partition.start" = "-6",
    "dynamic_partition.end" = "1",
    "dynamic_partition.prefix" = "p",
    "replication_num" = "1" -- 设置副本数为1，集群模式要设置为3
);

drop table if exists slr_rule_key_history;
CREATE TABLE IF NOT EXISTS slr_rule_key_history
(
    `RULE_CODE`     BIGINT      NOT NULL COMMENT '规则编号',
    `RULE_VERSION`  BIGINT      NOT NULL COMMENT '规则版本',
    `CHANNEL`       VARCHAR(16) NOT NULL COMMENT '渠道',
    `TARGET_FIELD`  VARCHAR(64) NOT NULL COMMENT '目标编号',
    `TARGET_VALUE`  VARCHAR(64) NOT NULL COMMENT '目标值'
)
    ENGINE = OLAP
    UNIQUE KEY(`RULE_CODE`, `RULE_VERSION`, `CHANNEL`, `TARGET_FIELD`, `TARGET_VALUE`)
DISTRIBUTED BY HASH(`RULE_CODE`, `RULE_VERSION`, `CHANNEL`, `TARGET_FIELD`) BUCKETS AUTO
PROPERTIES
(
    "enable_unique_key_merge_on_write" = "true", -- 写入时合并，提高查询性能
    "replication_num" = "1" -- 设置副本数为1，集群模式要设置为3
);
```
