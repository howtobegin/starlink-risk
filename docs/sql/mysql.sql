/*
 Navicat Premium Dump SQL

 Source Server         : lbs@localhost_mysql
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3308
 Source Schema         : starlink_risk

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 25/12/2024 16:55:36
*/

-- CREATE database `starlink_risk` CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
-- GRANT ALL PRIVILEGES ON `starlink_risk`.* TO 'lbs'@'%';
-- GRANT REPLICATION CLIENT ON *.* TO 'lbs'@'%';
-- GRANT REPLICATION SLAVE ON *.* TO 'lbs'@'%';
-- FLUSH PRIVILEGES;

DROP TABLE IF EXISTS `slr_rule_cond`;
CREATE TABLE `slr_rule_cond`
(
    `id`                     bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `rule_code`              varchar(64)          DEFAULT NULL COMMENT '规则编号',
    `event_code`             varchar(64)          DEFAULT NULL COMMENT '事件编号',
    `cond_code`              varchar(64)          DEFAULT NULL COMMENT '条件编号',
    `cond_type`              varchar(64)          DEFAULT NULL COMMENT '条件类型',
    `window_value`           bigint UNSIGNED      DEFAULT NULL COMMENT '窗口值',
    `window_unit`            varchar(64)          DEFAULT NULL COMMENT '窗口单位',
    `begin_time`             varchar(64)          DEFAULT NULL COMMENT '开始时间',
    `end_time`               varchar(64)          DEFAULT NULL COMMENT '结束时间',
    `threshold`              bigint               DEFAULT NULL COMMENT '阈值',
    `cross_history`          bit(1)               DEFAULT NULL COMMENT '条件类型',
    `cross_history_timeline` varchar(64)          DEFAULT NULL COMMENT '跨历史时间点',
    `creator`                varchar(64)          DEFAULT NULL COMMENT '创建用户',
    `updater`                varchar(64)          DEFAULT NULL COMMENT '更新用户',
    `create_time`            datetime        NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`            datetime        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则条件';

DROP TABLE IF EXISTS `slr_rule_event_attr`;
CREATE TABLE `slr_rule_event_attr`
(
    `id`             bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `event_code`     varchar(64)          DEFAULT NULL COMMENT '事件编号',
    `attribute_code` varchar(64)          DEFAULT NULL COMMENT '属性编号',
    `attribute_name` varchar(64)          DEFAULT NULL COMMENT '属性名称',
    `attribute_key`  varchar(64)          DEFAULT NULL COMMENT '属性key',
    `attribute_type` varchar(64)          DEFAULT NULL COMMENT '属性类型',
    `creator`        varchar(64)          DEFAULT NULL COMMENT '创建用户',
    `updater`        varchar(64)          DEFAULT NULL COMMENT '更新用户',
    `create_time`    datetime        NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则事件属性';

DROP TABLE IF EXISTS `slr_rule_event_attr_value`;
CREATE TABLE `slr_rule_event_attr_value`
(
    `id`              bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `cond_code`       varchar(64)          DEFAULT NULL COMMENT '条件编号',
    `attribute_code`  varchar(64)          DEFAULT NULL COMMENT '属性编号',
    `attribute_value` varchar(64)          DEFAULT NULL COMMENT '属性值',
    `attribute_op`    varchar(64)          DEFAULT NULL COMMENT '属性比较符',
    `creator`         varchar(64)          DEFAULT NULL COMMENT '创建用户',
    `updater`         varchar(64)          DEFAULT NULL COMMENT '更新用户',
    `create_time`     datetime        NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`     datetime        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则事件属性值表';

DROP TABLE IF EXISTS `slr_rule_event`;
CREATE TABLE `slr_rule_event`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `channel`     varchar(64)          DEFAULT NULL COMMENT '渠道',
    `eventCode`   varchar(64)          DEFAULT NULL COMMENT '事件编号',
    `eventName`   varchar(64)          DEFAULT NULL COMMENT '事件名称',
    `eventDesc`   varchar(64)          DEFAULT NULL COMMENT '事件描述',
    `creator`     varchar(64)          DEFAULT NULL COMMENT '创建用户',
    `updater`     varchar(64)          DEFAULT NULL COMMENT '更新用户',
    `create_time` datetime        NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则事件表';

DROP TABLE IF EXISTS `slr_rule_info`;
CREATE TABLE `slr_rule_info`
(
    `id`                 bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `channel`            varchar(64)          DEFAULT NULL COMMENT '渠道',
    `ruleCode`           varchar(64)          DEFAULT NULL COMMENT '规则编号',
    `ruleName`           varchar(64)          DEFAULT NULL COMMENT '规则名称',
    `ruleDesc`           varchar(64)          DEFAULT NULL COMMENT '规则描述',
    `ruleStatus`         varchar(64)          DEFAULT NULL COMMENT '规则状态',
    `alertIntervalValue` bigint UNSIGNED      DEFAULT NULL COMMENT '预警间隔值',
    `alertIntervalUnit`  varchar(64)          DEFAULT NULL COMMENT '预警间隔单位',
    `alertMessage`       text COMMENT '预警消息',
    `modelCode`          varchar(64)          DEFAULT NULL COMMENT '模型编号',
    `ruleCondCombOp`     varchar(64)          DEFAULT NULL COMMENT '条件组合符',
    `creator`            varchar(64)          DEFAULT NULL COMMENT '创建用户',
    `updater`            varchar(64)          DEFAULT NULL COMMENT '更新用户',
    `create_time`        datetime        NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`        datetime        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则信息表';

DROP TABLE IF EXISTS `slr_rule_json`;
CREATE TABLE `slr_rule_json`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `ruleCode` varchar(64) DEFAULT NULL COMMENT '规则编号',
    `ruleJson` text COMMENT '规则json',
    `creator`  varchar(64) DEFAULT NULL COMMENT '创建用户',
    `updater`  varchar(64) DEFAULT NULL COMMENT '更新用户',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则json表';

DROP TABLE IF EXISTS `slr_rule_model`;
CREATE TABLE `slr_rule_model`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `modelCode`   varchar(64)          DEFAULT NULL COMMENT '模型编号',
    `modelName`   varchar(64)          DEFAULT NULL COMMENT '模型名称',
    `modelDesc`   varchar(64)          DEFAULT NULL COMMENT '模型描述',
    `modelStatus` varchar(64)          DEFAULT NULL COMMENT '模型状态',
    `groovy`      text COMMENT '运算机groovy代码',
    `creator`     varchar(64)          DEFAULT NULL COMMENT '创建用户',
    `updater`     varchar(64)          DEFAULT NULL COMMENT '更新用户',
    `create_time` datetime        NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则模型表';

