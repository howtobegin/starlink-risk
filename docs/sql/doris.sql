CREATE DATABASE IF NOT EXISTS `starlink_risk`;

use `starlink_risk`;

CREATE TABLE IF NOT EXISTS slr_event
(
    `event_timestamp`    DATETIME     NOT NULL COMMENT '事件时间',
    `key_code`           VARCHAR(32)  NOT NULL COMMENT 'key编号',
    `key_value`          VARCHAR(32)  NOT NULL COMMENT 'key值',
    `event_code`         VARCHAR(32)  NOT NULL COMMENT '事件编号',
    `event_value`        VARCHAR(32)  NOT NULL COMMENT '事件值',
    `event_attribute`    MAP<STRING, STRING> NULL COMMENT '事件属性',
    `channel`            VARCHAR(16)  NOT NULL COMMENT '渠道'
)
ENGINE = OLAP
DUPLICATE KEY(`event_timestamp`, `key_code`, `key_value`)
PARTITION BY RANGE(`event_timestamp`) ()
DISTRIBUTED BY HASH(`event_timestamp`, `key_code`, `key_value`) BUCKETS AUTO
PROPERTIES
(
    "dynamic_partition.enable" = "true",
    "dynamic_partition.time_unit" = "MONTH",
    "dynamic_partition.start" = "-6",
    "dynamic_partition.end" = "1",
    "dynamic_partition.prefix" = "p"
);