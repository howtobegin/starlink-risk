# 进入 fe 容器内

```
docker-compose exec fe bash

mysql -h 192.168.6.186 -P 9030 -uroot

SET PASSWORD FOR 'root' = PASSWORD('Rongshu@2024');
use mysql;
ALTER SYSTEM ADD BACKEND "192.168.6.186:9050";
SHOW PROC '/backends';
CREATE DATABASE IF NOT EXISTS `starlink_risk`;
use starlink_risk;

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
    "replication_num" =
    "1" -- 设置副本数为1，集群模式要设置为3
);
```
