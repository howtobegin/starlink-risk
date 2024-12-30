# 进入 fe 容器内

```
docker-compose exec fe bash
mysql -h ${宿主机IP} -P 9030 -uroot
SET PASSWORD FOR 'root' = PASSWORD('Rongshu@2024');
```

# 浏览器打开`http://localhost:8030/` ，选择任意数据库，执行语句

```
ALTER SYSTEM ADD BACKEND "${宿主机IP}:9050";
SHOW PROC '/backends';

CREATE DATABASE IF NOT EXISTS `starlink_risk`;
# 手动选择新建的数据库
CREATE TABLE IF NOT EXISTS slr_event
(
    `EVENT_TIME`   DATETIME    NOT NULL COMMENT '事件时间',
    `TARGET_FILED`  VARCHAR(64) NOT NULL COMMENT 'key编号',
    `TARGET_VALUE` VARCHAR(64) NOT NULL COMMENT 'key值',
    `EVENT_FILED`   VARCHAR(64) NOT NULL COMMENT '事件编号',
    `EVENT_VALUE`  VARCHAR(64) NOT NULL COMMENT '事件值',
    `EVENT_ATTR_MAP` MAP< STRING,STRING> NULL COMMENT '事件属性',
    `CHANNEL`      VARCHAR(16) NOT NULL COMMENT '渠道'
)
ENGINE = OLAP
DUPLICATE KEY(`EVENT_TIME`, `TARGET_FILED`, `TARGET_VALUE`)
PARTITION BY RANGE(`EVENT_TIME`) ()
DISTRIBUTED BY HASH(`EVENT_TIME`, `TARGET_FILED`, `TARGET_VALUE`) BUCKETS AUTO
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
