CREATE DATABASE IF NOT EXISTS `starlink_risk`;

use `starlink_risk`;

CREATE TABLE IF NOT EXISTS slr_event
(
    `EVENT_TIME`    DATETIME     NOT NULL COMMENT '事件时间',
    `KEY_CODE`           VARCHAR(32)  NOT NULL COMMENT 'key编号',
    `KEY_VALUE`          VARCHAR(32)  NOT NULL COMMENT 'key值',
    `EVENT_CODE`         VARCHAR(32)  NOT NULL COMMENT '事件编号',
    `EVENT_VALUE`        VARCHAR(32)  NOT NULL COMMENT '事件值',
    `EVENT_ATTRIBUTE`    MAP<STRING, STRING> NULL COMMENT '事件属性',
    `CHANNEL`            VARCHAR(16)  NOT NULL COMMENT '渠道'
)
ENGINE = OLAP
DUPLICATE KEY(`EVENT_TIME`, `KEY_CODE`, `KEY_VALUE`)
PARTITION BY RANGE(`EVENT_TIME`) ()
DISTRIBUTED BY HASH(`EVENT_TIME`, `KEY_CODE`, `KEY_VALUE`) BUCKETS AUTO
PROPERTIES
(
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH",
    "dynamic_partition.start" = "-6",
    "dynamic_partition.end" = "1",
    "dynamic_partition.prefix" = "p",
    "replication_num" = "1"  -- 设置副本数为1，集群模式要设置为3
);