/*
 Navicat Premium Dump SQL

 Source Server         : mysql@localhost
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3308
 Source Schema         : starlink_risk

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 29/12/2024 13:08:21
*/

-- CREATE database `starlink_risk` CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
-- GRANT ALL PRIVILEGES ON `starlink_risk`.* TO 'lbs'@'%';
-- GRANT REPLICATION CLIENT ON *.* TO 'lbs'@'%';
-- GRANT REPLICATION SLAVE ON *.* TO 'lbs'@'%';
-- FLUSH PRIVILEGES;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for slr_rule_cond
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_cond`;
CREATE TABLE `slr_rule_cond`
(
    `id`                     bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `rule_code`              varchar(256)    DEFAULT NULL COMMENT '规则编号',
    `event_code`             varchar(256)    DEFAULT NULL COMMENT '事件编号',
    `cond_code`              varchar(256)    DEFAULT NULL COMMENT '条件编号',
    `cond_type`              varchar(32)     DEFAULT NULL COMMENT '条件类型',
    `window_value`           bigint UNSIGNED DEFAULT NULL COMMENT '窗口值',
    `window_unit`            varchar(32)     DEFAULT NULL COMMENT '窗口单位',
    `begin_time`             datetime        DEFAULT NULL COMMENT '开始时间',
    `end_time`               datetime        DEFAULT NULL COMMENT '结束时间',
    `threshold`              bigint          DEFAULT NULL COMMENT '阈值',
    `cross_history`          bit(1)          DEFAULT NULL COMMENT '条件类型',
    `cross_history_timeline` datetime        DEFAULT NULL COMMENT '跨历史时间点',
    `creator`                varchar(32)     DEFAULT NULL COMMENT '创建用户',
    `updater`                varchar(32)     DEFAULT NULL COMMENT '更新用户',
    `create_time`            datetime        DEFAULT NULL COMMENT '创建时间',
    `update_time`            datetime        DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则条件'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for slr_rule_event
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_event`;
CREATE TABLE `slr_rule_event`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `target_code` varchar(256) DEFAULT NULL COMMENT '目标编号',
    `event_code`  varchar(256) DEFAULT NULL COMMENT '事件编号',
    `event_name`  varchar(32)  DEFAULT NULL COMMENT '事件名称',
    `event_desc`  varchar(64)  DEFAULT NULL COMMENT '事件描述',
    `creator`     varchar(32)  DEFAULT NULL COMMENT '创建用户',
    `updater`     varchar(32)  DEFAULT NULL COMMENT '更新用户',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则事件表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for slr_rule_event_attr
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_event_attr`;
CREATE TABLE `slr_rule_event_attr`
(
    `id`             bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `event_code`     varchar(256) DEFAULT NULL COMMENT '事件编号',
    `attribute_code` varchar(256) DEFAULT NULL COMMENT '属性编号',
    `attribute_name` varchar(32)  DEFAULT NULL COMMENT '属性名称',
    `attribute_type` varchar(32)  DEFAULT NULL COMMENT '属性类型',
    `creator`        varchar(32)  DEFAULT NULL COMMENT '创建用户',
    `updater`        varchar(32)  DEFAULT NULL COMMENT '更新用户',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则事件属性'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for slr_rule_event_attr_value
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_event_attr_value`;
CREATE TABLE `slr_rule_event_attr_value`
(
    `id`              bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `cond_code`       varchar(256) DEFAULT NULL COMMENT '条件编号',
    `attribute_code`  varchar(256) DEFAULT NULL COMMENT '属性编号',
    `attribute_value` varchar(64)  DEFAULT NULL COMMENT '属性值',
    `attribute_op`    varchar(32)  DEFAULT NULL COMMENT '属性比较符',
    `creator`         varchar(32)  DEFAULT NULL COMMENT '创建用户',
    `updater`         varchar(32)  DEFAULT NULL COMMENT '更新用户',
    `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`     datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则事件属性值表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for slr_rule_info
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_info`;
CREATE TABLE `slr_rule_info`
(
    `id`                   bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `channel`              varchar(32)          DEFAULT NULL COMMENT '渠道',
    `rule_code`            varchar(256)         DEFAULT NULL COMMENT '规则编号',
    `rule_name`            varchar(32)          DEFAULT NULL COMMENT '规则名称',
    `rule_desc`            varchar(64)          DEFAULT NULL COMMENT '规则描述',
    `rule_status`          varchar(32)          DEFAULT NULL COMMENT '规则状态',
    `alert_interval_value` bigint UNSIGNED      DEFAULT NULL COMMENT '预警间隔值',
    `alert_interval_unit`  varchar(32)          DEFAULT NULL COMMENT '预警间隔单位',
    `alert_message`        text COMMENT '预警消息',
    `target_code`          varchar(256)         DEFAULT NULL COMMENT '目标编号',
    `model_code`           varchar(256)         DEFAULT NULL COMMENT '模型编号',
    `rule_cond_comb_op`    varchar(32)          DEFAULT NULL COMMENT '条件组合符',
    `creator`              varchar(32)          DEFAULT NULL COMMENT '创建用户',
    `updater`              varchar(32)          DEFAULT NULL COMMENT '更新用户',
    `create_time`          datetime        NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`          datetime        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for slr_rule_json
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_json`;
CREATE TABLE `slr_rule_json`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `rule_code`   varchar(256)         DEFAULT NULL COMMENT '规则编号',
    `rule_json`   text COMMENT '规则json',
    `creator`     varchar(32)          DEFAULT NULL COMMENT '创建用户',
    `updater`     varchar(32)          DEFAULT NULL COMMENT '更新用户',
    `create_time` datetime        NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则json表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for slr_rule_target
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_target`;
CREATE TABLE `slr_rule_target`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `channel`     varchar(32)  DEFAULT NULL COMMENT '渠道',
    `target_code` varchar(256) DEFAULT NULL COMMENT '目标编号',
    `target_name` varchar(32)  DEFAULT NULL COMMENT '目标名称',
    `target_desc` varchar(64)  DEFAULT NULL COMMENT '目标描述',
    `creator`     varchar(32)  DEFAULT NULL COMMENT '创建用户',
    `updater`     varchar(32)  DEFAULT NULL COMMENT '更新用户',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则目标表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for slr_rule_model
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_model`;
CREATE TABLE `slr_rule_model`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `model_code`  varchar(256)         DEFAULT NULL COMMENT '模型编号',
    `model_name`  varchar(32)          DEFAULT NULL COMMENT '模型名称',
    `model_desc`  varchar(64)          DEFAULT NULL COMMENT '模型描述',
    `groovy`      text COMMENT '运算机groovy代码',
    `creator`     varchar(32)          DEFAULT NULL COMMENT '创建用户',
    `updater`     varchar(32)          DEFAULT NULL COMMENT '更新用户',
    `create_time` datetime        NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime        NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '风控规则模型表'
  ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
