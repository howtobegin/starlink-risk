/*
 Navicat Premium Dump SQL

 Source Server         : lbs@vm1
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : 192.168.161.128:3308
 Source Schema         : starlink_risk

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 26/08/2024 17:28:43
*/

# CREATE database `starlink_risk` CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
# GRANT ALL PRIVILEGES ON `starlink_risk`.* TO 'lbs'@'%';
# GRANT REPLICATION CLIENT ON *.* TO 'lbs'@'%';
# GRANT REPLICATION SLAVE ON *.* TO 'lbs'@'%';
# FLUSH PRIVILEGES;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for slr_event_attribute
-- ----------------------------
DROP TABLE IF EXISTS `slr_event_attribute`;
CREATE TABLE `slr_event_attribute`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `event_code`  varchar(64)     NOT NULL DEFAULT '' COMMENT '事件编号',
    `field_name`  varchar(64)     NOT NULL DEFAULT '' COMMENT '字段名称',
    `field_desc`  varchar(64)     NOT NULL DEFAULT '' COMMENT '字段描述',
    `creator`     varchar(255)    NOT NULL DEFAULT '' COMMENT '创建用户',
    `updater`     varchar(255)    NOT NULL DEFAULT '' COMMENT '更新用户',
    `create_time` datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `deleted`     bit(1)          NOT NULL DEFAULT b'0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '星链风控事件属性'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of slr_event_attribute
-- ----------------------------
INSERT INTO `slr_event_attribute`
VALUES (1, 'eventCode01', 'campaignId', '活动ID', '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');
INSERT INTO `slr_event_attribute`
VALUES (2, 'eventCode01', 'campaignName', '活动名称', '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');
INSERT INTO `slr_event_attribute`
VALUES (3, 'eventCode02', 'campaignId', '活动ID', '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');
INSERT INTO `slr_event_attribute`
VALUES (4, 'eventCode02', 'campaignName', '活动名称', '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');

-- ----------------------------
-- Table structure for slr_event_info
-- ----------------------------
DROP TABLE IF EXISTS `slr_event_info`;
CREATE TABLE `slr_event_info`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `event_code`  varchar(64)     NOT NULL DEFAULT '' COMMENT '事件编号',
    `channel`     varchar(64)     NOT NULL DEFAULT '' COMMENT '渠道',
    `event_name`  varchar(64)     NOT NULL DEFAULT '' COMMENT '事件名称',
    `event_desc`  varchar(255)    NOT NULL DEFAULT '' COMMENT '事件描述',
    `creator`     varchar(255)    NOT NULL DEFAULT '' COMMENT '创建用户',
    `updater`     varchar(255)    NOT NULL DEFAULT '' COMMENT '更新用户',
    `create_time` datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `deleted`     bit(1)          NOT NULL DEFAULT b'0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '星链风控事件信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of slr_event_info
-- ----------------------------
INSERT INTO `slr_event_info`
VALUES (1, 'eventCode01', 'game', '抽奖', '游戏抽奖', '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');
INSERT INTO `slr_event_info`
VALUES (2, 'eventCode02', 'game', '充值', '游戏充值', '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');

-- ----------------------------
-- Table structure for slr_rule_condition
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_condition`;
CREATE TABLE `slr_rule_condition`
(
    `id`                     bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `rule_code`              varchar(64)     NOT NULL DEFAULT '' COMMENT '规则编号',
    `event_code`             varchar(64)     NOT NULL DEFAULT '' COMMENT '事件编号',
    `event_threshold`        bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '事件阈值',
    `condition_type`         tinyint         NOT NULL DEFAULT 0 COMMENT '条件类型：0-固定范围条件；1-滑动窗口条件',
    `begin_time`             datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '固定范围开始时间',
    `end_time`               datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '固定范围结束时间',
    `window_size_value`      bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '滑动窗口大小值（仅用于前端展示）',
    `window_size_unit`       tinyint         NOT NULL DEFAULT 0 COMMENT '滑动窗口大小单位（仅用于前端展示）: 0-MILLISECOND; 1-SECOND; 2-MINUTE; 3-HOUR; 4-DAY; 5-WEEK; 6-MONTH; 7-YEAR',
    `window_size`            bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '滑动窗口大小',
    `is_cross_history`       bit(1)          NOT NULL DEFAULT b'1' COMMENT '是否跨历史（目前仅对范围规则生效，周期规则固定跨历史）：0-否，1-是',
    `cross_history_timeline` datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '跨历史时间点',
    `creator`                varchar(255)    NOT NULL DEFAULT '' COMMENT '创建用户',
    `updater`                varchar(255)    NOT NULL DEFAULT '' COMMENT '更新用户',
    `create_time`            datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `update_time`            datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `deleted`                bit(1)          NOT NULL DEFAULT b'0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '星链风控规则条件信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of slr_rule_condition
-- ----------------------------
INSERT INTO `slr_rule_condition`
VALUES (1, 'ruleCode01', 'eventCode01', 10, 1, '1970-01-01 00:00:00', '1970-01-01 00:00:00', 20, 2, 1200000, b'1',
        '2024-08-26 16:41:40', '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');
INSERT INTO `slr_rule_condition`
VALUES (2, 'ruleCode01', 'eventCode02', 20, 1, '1970-01-01 00:00:00', '1970-01-01 00:00:00', 30, 2, 1800000, b'1',
        '2024-08-26 16:41:40', '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');

-- ----------------------------
-- Table structure for slr_rule_info
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_info`;
CREATE TABLE `slr_rule_info`
(
    `id`                          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `channel`                     varchar(64)     NOT NULL DEFAULT '' COMMENT '渠道',
    `rule_code`                   varchar(64)     NOT NULL DEFAULT '' COMMENT '规则编号',
    `model_code`                  varchar(64)     NOT NULL DEFAULT '' COMMENT '模型编号',
    `rule_name`                   varchar(64)     NOT NULL DEFAULT '' COMMENT '规则名称',
    `rule_desc`                   varchar(255)    NOT NULL DEFAULT '' COMMENT '规则描述',
    `expire_begin_time`           datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '有效开始时间',
    `expire_end_time`             datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '有效结束时间',
    `combined_condition_operator` tinyint         NOT NULL DEFAULT 0 COMMENT '规则条件组合操作符: 0-and；1-or',
    `warn_message`                text            NOT NULL COMMENT '预警信息',
    `warn_interval_value`         bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '预警间隔值（仅用于前端展示）',
    `warn_interval_unit`          varchar(64)     NOT NULL DEFAULT '' COMMENT '预警间隔单位（仅用于前端展示）',
    `warn_interval`               bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '预警间隔',
    `status`                      tinyint         NOT NULL DEFAULT 0 COMMENT '状态：0-停用，1-启用',
    `creator`                     varchar(255)    NOT NULL DEFAULT '' COMMENT '创建用户',
    `updater`                     varchar(255)    NOT NULL DEFAULT '' COMMENT '更新用户',
    `create_time`                 datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `update_time`                 datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `deleted`                     bit(1)          NOT NULL DEFAULT b'0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '星链风控规则基本信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of slr_rule_info
-- ----------------------------
INSERT INTO `slr_rule_info`
VALUES (1, 'game', 'ruleCode01', 'modelCode01', 'ruleName01', 'ruleDesc01', '2024-08-26 16:36:03',
        '2024-09-26 16:36:13', 0, '高频抽奖触发', 5, '2', 300000, 1, '', '', '1970-01-01 00:00:00',
        '1970-01-01 00:00:00', b'0');

-- ----------------------------
-- Table structure for slr_rule_json
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_json`;
CREATE TABLE `slr_rule_json`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `rule_code`   varchar(64)     NOT NULL DEFAULT '' COMMENT '规则编号',
    `rule_json`   longtext        NOT NULL COMMENT '规则json',
    `creator`     varchar(255)    NOT NULL DEFAULT '' COMMENT '创建用户',
    `updater`     varchar(255)    NOT NULL DEFAULT '' COMMENT '更新用户',
    `create_time` datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `deleted`     bit(1)          NOT NULL DEFAULT b'0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '星链风控规则json数据'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of slr_rule_json
-- ----------------------------
INSERT INTO `slr_rule_json`
VALUES (1, 'ruleCode01',
        '{\n  \"channel\": \"game\",\n  \"ruleCode\": \"ruleCode01\",\n  \"modelCode\": \"modelCode01\",\n  \"ruleName\": \"ruleName01\",\n  \"ruleDesc\": \"ruleDesc01\",\n  \"combinedConditionOperator\": 0,\n  \"warnMessage\": \"高频抽奖触发\",\n  \"warnInterval\": 300000,\n  \"status\": 1,\n  \"ruleConditionGroup\": [\n    {\n      \"ruleCode\": \"ruleCode01\",\n      \"eventCode\": \"eventCode01\",\n      \"eventThreshold\": 10,\n      \"conditionType\": 1,\n      \"beginTime\": \"1970-01-01 00:00:00\",\n      \"endTime\": \"1970-01-01 00:00:00\",\n      \"windowSize\": 1200000,\n      \"isCrossHistory\": true,\n      \"crossHistoryTimeline\": \"2024-08-26 16:41:40\",\n      \"eventInfo\": {\n        \"eventCode\": \"eventCode01\",\n        \"channel\": \"game\",\n        \"eventName\": \"抽奖\",\n        \"eventDesc\": \"游戏抽奖\",\n        \"eventAttributeGroup\": [\n          {\n            \"eventCode\": \"eventCode01\",\n            \"fieldName\": \"campaignId\",\n            \"fieldDesc\": \"活动ID\"\n          },\n          {\n            \"eventCode\": \"eventCode01\",\n            \"fieldName\": \"campaignName\",\n            \"fieldDesc\": \"活动名称\"\n          }\n        ]\n      }\n    },\n    {\n      \"ruleCode\": \"ruleCode01\",\n      \"eventCode\": \"eventCode02\",\n      \"eventThreshold\": 20,\n      \"conditionType\": 1,\n      \"beginTime\": \"1970-01-01 00:00:00\",\n      \"endTime\": \"1970-01-01 00:00:00\",\n      \"windowSize\": 1800000,\n      \"isCrossHistory\": true,\n      \"crossHistoryTimeline\": \"2024-08-26 16:41:40\",\n      \"eventInfo\": {\n        \"eventCode\": \"eventCode02\",\n        \"channel\": \"game\",\n        \"eventName\": \"充值\",\n        \"eventDesc\": \"游戏充值\",\n        \"eventAttributeGroup\": [\n          {\n            \"eventCode\": \"eventCode02\",\n            \"fieldName\": \"campaignId\",\n            \"fieldDesc\": \"活动ID\"\n          },\n          {\n            \"eventCode\": \"eventCode02\",\n            \"fieldName\": \"campaignName\",\n            \"fieldDesc\": \"活动名称\"\n          }\n        ]\n      }\n    }\n  ],\n  \"ruleModelGroovyCode\": \"package com.liboshuai.starlink.slr.engine.processor.impl;\\n\\nimport com.liboshuai.starlink.slr.engine.api.constants.GlobalConstants;\\nimport com.liboshuai.starlink.slr.engine.api.constants.RedisKeyConstants;\\nimport com.liboshuai.starlink.slr.engine.api.dto.EventKafkaDTO;\\nimport com.liboshuai.starlink.slr.engine.api.dto.RuleConditionDTO;\\nimport com.liboshuai.starlink.slr.engine.api.dto.RuleInfoDTO;\\nimport com.liboshuai.starlink.slr.engine.api.enums.RuleConditionOperatorTypeEnum;\\nimport com.liboshuai.starlink.slr.engine.dto.RuleCdcDTO;\\nimport com.liboshuai.starlink.slr.engine.exception.BusinessException;\\nimport com.liboshuai.starlink.slr.engine.processor.Processor;\\nimport com.liboshuai.starlink.slr.engine.utils.data.RedisUtil;\\nimport com.liboshuai.starlink.slr.engine.utils.date.DateUtil;\\nimport com.liboshuai.starlink.slr.engine.utils.string.StringUtil;\\nimport lombok.extern.slf4j.Slf4j;\\nimport org.apache.flink.api.common.functions.RuntimeContext;\\nimport org.apache.flink.api.common.state.MapState;\\nimport org.apache.flink.api.common.state.MapStateDescriptor;\\nimport org.apache.flink.api.common.state.ValueState;\\nimport org.apache.flink.api.common.state.ValueStateDescriptor;\\nimport org.apache.flink.api.common.typeinfo.Types;\\nimport org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;\\nimport org.apache.flink.util.CollectionUtil;\\nimport org.apache.flink.util.Collector;\\nimport org.apache.flink.util.StringUtils;\\n\\nimport java.time.LocalDateTime;\\nimport java.util.*;\\n\\n/**\\n * 运算机one\\n */\\n@Slf4j\\npublic class ProcessorOne implements Processor {\\n\\n    /**\\n     * 规则信息\\n     */\\n    private ValueState<RuleInfoDTO> ruleInfoDTOValueState;\\n\\n    /**\\n     * smallValue（窗口步长）: key为eventCode,value为eventValue\\n     */\\n    private MapState<String, Long> smallMapState;\\n\\n    /**\\n     * bigValue（窗口大小）: key为eventCode，value为一个一个步长的eventValue累加值\\n     */\\n    private MapState<String, List<Long>> bigMapState;\\n\\n    /**\\n     * 最近一次预警时间\\n     */\\n    private ValueState<Long> lastWarningTimeState;\\n\\n    @Override\\n    public void open(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) {\\n        String ruleCode = ruleInfoDTO.getRuleCode();\\n        ruleInfoDTOValueState = runtimeContext.getState(\\n                new ValueStateDescriptor<>(\\\"ruleInfoDTOValueState_\\\" + ruleCode, RuleInfoDTO.class)\\n        );\\n        smallMapState = runtimeContext.getMapState(\\n                new MapStateDescriptor<>(\\\"smallMapState_\\\" + ruleCode, String.class, Long.class)\\n        );\\n        bigMapState = runtimeContext.getMapState(\\n                new MapStateDescriptor<>(\\\"bigMapState_\\\" + ruleCode, Types.STRING, Types.LIST(Types.LONG))\\n        );\\n        lastWarningTimeState = runtimeContext.getState(\\n                new ValueStateDescriptor<>(\\\"lastWarningTimeState_\\\" + ruleCode, Long.class)\\n        );\\n    }\\n\\n    @Override\\n    public void processElement(EventKafkaDTO eventKafkaDTO, Collector<String> out) throws Exception {\\n        RuleInfoDTO ruleInfoDTO = ruleInfoDTOValueState.value();\\n        if (Objects.isNull(ruleInfoDTO)) {\\n            throw new BusinessException(\\\"运算机 ruleInfoDTO 必须非空\\\");\\n        }\\n        // 获取当前事件时间戳\\n        String timestamp = eventKafkaDTO.getTimestamp();\\n        LocalDateTime eventTime = DateUtil.convertTimestamp2LocalDateTime(Long.parseLong(timestamp));\\n        // 获取规则条件\\n        List<RuleConditionDTO> ruleConditionList = ruleInfoDTO.getRuleConditionGroup();\\n        if (CollectionUtil.isNullOrEmpty(ruleConditionList)) {\\n            throw new BusinessException(\\\"运算机 ruleConditionList 必须非空\\\");\\n        }\\n        // 多个规则条件进行窗口值累加\\n        for (RuleConditionDTO ruleConditionDTO : ruleConditionList) {\\n            // 划分为跨历史时间段 和 不跨历史时间段\\n            if (ruleConditionDTO.getIsCrossHistory()) { // 跨历史时间段\\n                LocalDateTime crossHistoryTimeline = ruleConditionDTO.getCrossHistoryTimeline();\\n                // 匹配到事件时，进行事件值累加\\n                if (Objects.equals(eventKafkaDTO.getEventCode(), ruleConditionDTO.getEventCode())\\n                        && eventTime.isAfter(crossHistoryTimeline)) {\\n                    if (smallMapState.get(eventKafkaDTO.getEventCode()) == null) {\\n                        // 跨历史时间段，当状态值为空时从redis获取初始值\\n                        String key = RedisKeyConstants.DORIS_HISTORY_VALUE\\n                                + GlobalConstants.REDIS_KEY_SEPARATOR + ruleConditionDTO.getRuleCode()\\n                                + GlobalConstants.REDIS_KEY_SEPARATOR + ruleConditionDTO.getEventCode();\\n                        String keyCode = eventKafkaDTO.getKeyCode();\\n                        String initValue = RedisUtil.hget(key, keyCode);\\n                        if (StringUtils.isNullOrWhitespaceOnly(initValue)) {\\n                            throw new BusinessException(StringUtil.format(\\\"从redis获取初始值必须非空, key:{}, hashKey: {}\\\", key, keyCode));\\n                        }\\n                        smallMapState.put(eventKafkaDTO.getEventCode(), Long.parseLong(initValue));\\n                    }\\n                    smallMapState.put(eventKafkaDTO.getEventCode(),\\n                            smallMapState.get(eventKafkaDTO.getEventCode()) + Long.parseLong(eventKafkaDTO.getEventValue()));\\n                }\\n            } else { // 非跨历史时间段\\n                // 匹配到事件时，进行事件值累加\\n                if (Objects.equals(eventKafkaDTO.getEventCode(), ruleConditionDTO.getEventCode())) {\\n                    if (smallMapState.get(eventKafkaDTO.getEventCode()) == null) {\\n                        // 非跨历史时间段，当状态值为空时直接初始化为0\\n                        smallMapState.put(eventKafkaDTO.getEventCode(), 0L);\\n                    }\\n                    smallMapState.put(eventKafkaDTO.getEventCode(),\\n                            smallMapState.get(eventKafkaDTO.getEventCode()) + Long.parseLong(eventKafkaDTO.getEventValue()));\\n                }\\n            }\\n        }\\n    }\\n\\n    @Override\\n    public void onTimer(long timestamp, KeyedBroadcastProcessFunction<String, EventKafkaDTO, RuleCdcDTO, String>.OnTimerContext ctx, Collector<String> out) throws Exception {\\n        RuleInfoDTO ruleInfoDTO = ruleInfoDTOValueState.value();\\n        if (Objects.isNull(ruleInfoDTO)) {\\n            throw new BusinessException(\\\"运算机 ruleInfoDTO 必须非空\\\");\\n        }\\n        // 获取规则条件\\n        List<RuleConditionDTO> ruleConditionList = ruleInfoDTO.getRuleConditionGroup();\\n        if (CollectionUtil.isNullOrEmpty(ruleConditionList)) {\\n            throw new BusinessException(\\\"运算机 ruleConditionList 必须非空\\\");\\n        }\\n        // 将规则条件根据事件编号存储到map中，方便后续操作\\n        Map<String, RuleConditionDTO> ruleConditionMapByEventCode = new HashMap<>();\\n        for (RuleConditionDTO ruleConditionDTO : ruleConditionList) {\\n            ruleConditionMapByEventCode.put(ruleConditionDTO.getEventCode(), ruleConditionDTO);\\n        }\\n        // 将smallMapState的值临时转移到普通的smallMap中，方便数据操作\\n        Map<String, Long> smallMap = new HashMap<>();\\n        Iterator<Map.Entry<String, Long>> smallIterator = smallMapState.iterator();\\n        while (smallIterator.hasNext()) {\\n            Map.Entry<String, Long> next = smallIterator.next();\\n            smallMap.put(next.getKey(), next.getValue());\\n        }\\n        // 将bigMapState的值临时转移到普通的bigMap中，方便数据操作\\n        Map<String, List<Long>> bigMap = new HashMap<>();\\n        Iterator<Map.Entry<String, List<Long>>> bigIterator = bigMapState.iterator();\\n        while (bigIterator.hasNext()) {\\n            Map.Entry<String, List<Long>> next = bigIterator.next();\\n            bigMap.put(next.getKey(), next.getValue());\\n        }\\n        // 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMap中\\n        for (Map.Entry<String, Long> smallMapEntry : smallMap.entrySet()) {\\n            String eventCode = smallMapEntry.getKey();\\n            Long eventValue = smallMapEntry.getValue();\\n            List<Long> oldEventValueList = bigMap.get(eventCode);\\n            if (CollectionUtil.isNullOrEmpty(oldEventValueList)) {\\n                oldEventValueList = new ArrayList<>();\\n            }\\n            oldEventValueList.add(eventValue);\\n            bigMap.put(eventCode, oldEventValueList);\\n        }\\n        // 当前窗口步长的数据已经添加到窗口中了，清空状态\\n        smallMapState.clear();\\n        // 清理窗口大小之外的数据\\n        for (Map.Entry<String, List<Long>> bigMapEntry : bigMap.entrySet()) {\\n            String eventCode = bigMapEntry.getKey();\\n            List<Long> eventValueList = bigMapEntry.getValue();\\n            Long windowSize = ruleConditionMapByEventCode.get(eventCode).getWindowSize();\\n            if (eventValueList.size() > windowSize) {\\n                eventValueList = eventValueList.subList(eventValueList.size() - 20, eventValueList.size());\\n            }\\n            bigMap.put(eventCode, eventValueList);\\n        }\\n        // 将bigMap更新到bigMapState中\\n        for (Map.Entry<String, List<Long>> bigMapEntry : bigMap.entrySet()) {\\n            String eventCode = bigMapEntry.getKey();\\n            List<Long> eventValueList = bigMapEntry.getValue();\\n            bigMapState.put(eventCode, eventValueList);\\n        }\\n        // 判断是否触发规则事件阈值\\n        Map<String, Boolean> eventCodeAndWarnResult = new HashMap<>();\\n        for (Map.Entry<String, List<Long>> bigMapEntry : bigMap.entrySet()) {\\n            String eventCode = bigMapEntry.getKey();\\n            List<Long> eventValueList = bigMapEntry.getValue();\\n            long eventValueSum = eventValueList.stream().mapToLong(Long::longValue).sum();\\n            Long eventThreshold = ruleConditionMapByEventCode.get(eventCode).getEventThreshold();\\n            eventCodeAndWarnResult.put(eventCode, eventValueSum > eventThreshold);\\n        }\\n        Integer conditionOperator = ruleInfoDTO.getCombinedConditionOperator();\\n        // 根据规则中事件条件表达式组合判断事件结果 与预警频率 判断否是触发预警\\n        boolean eventResult = evaluateEventResults(eventCodeAndWarnResult, conditionOperator);\\n        if (eventResult && (timestamp - lastWarningTimeState.value() >= ruleInfoDTO.getWarnInterval())) {\\n            lastWarningTimeState.update(timestamp);\\n            // TODO: 进行预警信息拼接组合\\n            out.collect(\\\"事件[{}]触发了[{}]规则，事件值超过阈值[{}]，请尽快处理\\\");\\n        }\\n    }\\n\\n    public static boolean evaluateEventResults(Map<String, Boolean> eventCodeAndWarnResult, Integer conditionOperator) {\\n        // 初始化结果变量\\n        boolean result = conditionOperator.equals(RuleConditionOperatorTypeEnum.AND.getCode());\\n\\n        // 遍历 Map\\n        for (Boolean eventResult : eventCodeAndWarnResult.values()) {\\n            if (conditionOperator.equals(RuleConditionOperatorTypeEnum.AND.getCode())) {\\n                // 对于 AND，只有当所有结果都为 true 时，结果才为 true\\n                result = eventResult;\\n                // 提前结束循环，如果结果已经为 false\\n                if (!result) {\\n                    break;\\n                }\\n            } else if (conditionOperator.equals(RuleConditionOperatorTypeEnum.OR.getCode())) {\\n                // 对于 OR，只要有一个结果为 true，结果就为 true\\n                result = eventResult;\\n                // 提前结束循环，如果结果已经为 true\\n                if (result) {\\n                    break;\\n                }\\n            }\\n        }\\n\\n        return result;\\n    }\\n}\\n\"\n}',
        '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');

-- ----------------------------
-- Table structure for slr_rule_model
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_model`;
CREATE TABLE `slr_rule_model`
(
    `id`          bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `model_code`  varchar(64)     NOT NULL DEFAULT '' COMMENT '模型编号',
    `groovy`      longtext        NOT NULL COMMENT '规则模型groovy代码',
    `version`     bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '模型版本号',
    `creator`     varchar(255)    NOT NULL DEFAULT '' COMMENT '创建用户',
    `updater`     varchar(255)    NOT NULL DEFAULT '' COMMENT '更新用户',
    `create_time` datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `update_time` datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `deleted`     bit(1)          NOT NULL DEFAULT b'0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '星链风控规则模型'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of slr_rule_model
-- ----------------------------
INSERT INTO slr_rule_model (id, model_code, groovy, version, creator, updater, create_time, update_time, deleted) VALUES (1, 'modelCode01', 'package com.liboshuai.starlink.slr.engine.processor.impl\n\nimport com.liboshuai.starlink.slr.engine.api.constants.GlobalConstants\nimport com.liboshuai.starlink.slr.engine.api.constants.RedisKeyConstants\nimport com.liboshuai.starlink.slr.engine.api.dto.EventKafkaDTO\nimport com.liboshuai.starlink.slr.engine.api.dto.ProcessorDTO\nimport com.liboshuai.starlink.slr.engine.api.dto.RuleConditionDTO\nimport com.liboshuai.starlink.slr.engine.api.dto.RuleInfoDTO\nimport com.liboshuai.starlink.slr.engine.api.enums.RuleConditionOperatorTypeEnum\nimport com.liboshuai.starlink.slr.engine.api.util.TemplatePlaceholderUtil\nimport com.liboshuai.starlink.slr.engine.exception.BusinessException\nimport com.liboshuai.starlink.slr.engine.processor.Processor\nimport com.liboshuai.starlink.slr.engine.utils.data.RedisUtil\nimport com.liboshuai.starlink.slr.engine.utils.date.DateUtil\nimport com.liboshuai.starlink.slr.engine.utils.string.JsonUtil\nimport com.liboshuai.starlink.slr.engine.utils.string.StringUtil\nimport org.apache.flink.api.common.functions.RuntimeContext\nimport org.apache.flink.api.common.state.MapState\nimport org.apache.flink.api.common.state.MapStateDescriptor\nimport org.apache.flink.api.common.state.ValueState\nimport org.apache.flink.api.common.state.ValueStateDescriptor\nimport org.apache.flink.api.common.typeinfo.Types\nimport org.apache.flink.api.java.tuple.Tuple2\nimport org.apache.flink.util.Collector\nimport org.apache.flink.util.StringUtils\nimport org.slf4j.Logger\nimport org.slf4j.LoggerFactory\n\nimport java.time.LocalDateTime\n\n/**\n * 运算机one\n */\nclass ProcessorOne implements Processor {\n\n    private static final Logger log = LoggerFactory.getLogger(ProcessorOne.class)\n\n    /**\n     * smallValue（窗口步长）: key为eventCode,value为eventValue和最新的EventKafkaDTO\n     */\n    private MapState<String, Tuple2<Long, EventKafkaDTO>> smallMapState\n\n    /**\n     * 记录对应eventCode是否已经初始化过（注意不要使用ListState，它查找指定元素的性能很差）\n     */\n    private MapState<String, Boolean> smallInitMapState\n\n    /**\n     * bigValue（窗口大小）: key为eventCode，小map的key为时间戳，小map的value为一个一个步长的eventValue累加值和最新的EventKafkaDTO\n     */\n    private MapState<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapState\n\n    /**\n     * 最近一次预警时间\n     */\n    private ValueState<Long> lastWarningTimeState\n\n    @Override\n    void init(RuntimeContext runtimeContext, RuleInfoDTO ruleInfoDTO) {\n        String ruleCode = ruleInfoDTO.getRuleCode()\n        // 状态变量注册使用 ruleCode 作为后缀，以防止不同规则使用相同的模型导致状态变量数据冲突覆盖\n        smallMapState = runtimeContext.getMapState(\n                new MapStateDescriptor<>(\n                        \"smallMapState_${ruleCode}\", Types.STRING,\n                        Types.TUPLE(Types.LONG, Types.POJO(EventKafkaDTO.class))\n                )\n        )\n        smallInitMapState = runtimeContext.getMapState(\n                new MapStateDescriptor<>(\"smallInitMapState_${ruleCode}\", Types.STRING, Types.BOOLEAN)\n        )\n        bigMapState = runtimeContext.getMapState(\n                new MapStateDescriptor<>(\"bigMapState_${ruleCode}\", Types.STRING,\n                        Types.MAP(Types.LONG, Types.TUPLE(Types.LONG, Types.POJO(EventKafkaDTO.class))))\n        )\n        lastWarningTimeState = runtimeContext.getState(\n                new ValueStateDescriptor<>(\"lastWarningTimeState_${ruleCode}\", Types.LONG)\n        )\n    }\n\n    @Override\n    void processElement(long timestamp, EventKafkaDTO eventKafkaDTO, RuleInfoDTO ruleInfoDTO, Collector<String> out)\n            throws Exception {\n        if (Objects.isNull(ruleInfoDTO)) {\n            throw new BusinessException(\"运算机 ruleInfoDTO 必须非空\")\n        }\n        String eventKafkaDTOChannel = eventKafkaDTO.getChannel()\n        String ruleInfoChannel = ruleInfoDTO.getChannel()\n        if (!Objects.equals(eventKafkaDTOChannel, ruleInfoChannel)) {\n            return\n        }\n        // 获取规则条件\n        List<RuleConditionDTO> ruleConditionList = ruleInfoDTO.getRuleConditionGroup()\n        if (ruleConditionList == null || ruleConditionList.isEmpty()) {\n            throw new BusinessException(\"运算机 ruleConditionList 必须非空\")\n        }\n        // 多个规则条件进行窗口值累加\n        for (RuleConditionDTO ruleConditionDTO : ruleConditionList) {\n            // 事件编号匹配不上，则直接跳过\n            if (!Objects.equals(eventKafkaDTO.getEventCode(), ruleConditionDTO.getEventCode())) {\n                continue\n            }\n            // 状态值防空\n            if (smallMapState.get(eventKafkaDTO.getEventCode()) == null) {\n                smallMapState.put(eventKafkaDTO.getEventCode(), Tuple2.of(0L, eventKafkaDTO))\n            }\n            if (ruleConditionDTO.getIsCrossHistory()) { //跨历史时间段\n                LocalDateTime crossHistoryTimeline = ruleConditionDTO.getCrossHistoryTimeline()\n                // 因为跨历史时间段的规则条件需要处理历史缓存的数据，而历史缓存的数据可能过多，所以需要根据历史截止点进行过滤，仅需要大于历史截止点的数据\n                if (eventKafkaDTO.getTimestamp() <= DateUtil.convertLocalDateTime2Timestamp(crossHistoryTimeline)) {\n                    continue\n                }\n                // 因为跨历史时间段的规则条件需要从redis中获取doris中历史事件值，所以检查当前值是否已经通过redis初始化后，防止重复初始化\n                if (!smallInitMapState.contains(eventKafkaDTO.getEventCode())) {\n                    // 如果为跨历史时间段的，且还没有初始化，则需要从redis中获取初始值（注意：Groovy字符串拼接的方式很麻烦，故使用StringBuilder）\n                    String key = new StringBuilder().append(RedisKeyConstants.DORIS_HISTORY_VALUE)\n                            .append(GlobalConstants.REDIS_KEY_SEPARATOR)\n                            .append(ruleConditionDTO.getRuleCode())\n                            .append(GlobalConstants.REDIS_KEY_SEPARATOR)\n                            .append(ruleConditionDTO.getEventCode())\n                    String keyCode = eventKafkaDTO.getKeyCode()\n                    // 注意：因为上面获取历史缓存数据时，使用的是 <= 所以 redis 存储值时查询 doris 要包含历史截至时间点\n                    String initValue = RedisUtil.hget(key, keyCode)\n                    if (StringUtils.isNullOrWhitespaceOnly(initValue)) {\n                        throw new BusinessException(StringUtil.format(\"从redis获取初始值必须非空, key:{}, hashKey: {}\", key, keyCode))\n                    }\n                    smallMapState.put(eventKafkaDTO.getEventCode(), Tuple2.of(Long.parseLong(initValue), eventKafkaDTO))\n                    smallInitMapState.put(eventKafkaDTO.getEventCode(), true)\n                }\n                // 从redis初始化值后，正常处理数据\n                Tuple2<Long, EventKafkaDTO> currentTuple = smallMapState.get(eventKafkaDTO.getEventCode())\n                Long newValue = currentTuple.f0 + Long.parseLong(eventKafkaDTO.getEventValue())\n                smallMapState.put(eventKafkaDTO.getEventCode(), Tuple2.of(newValue, eventKafkaDTO))\n            } else { // 非跨历史时间段\n                // 对于非跨历史时间段，只处理当前一条数据，不需要处理历史缓存数据\n                if (eventKafkaDTO.getTimestamp() != timestamp) {\n                    continue\n                }\n                Tuple2<Long, EventKafkaDTO> currentTuple = smallMapState.get(eventKafkaDTO.getEventCode())\n                Long newValue = currentTuple.f0 + Long.parseLong(eventKafkaDTO.getEventValue())\n                smallMapState.put(eventKafkaDTO.getEventCode(), Tuple2.of(newValue, eventKafkaDTO))\n            }\n        }\n    }\n\n    @Override\n    void onTimer(long timestamp, RuleInfoDTO ruleInfoDTO, Collector<String> out) throws Exception {\n        if (Objects.isNull(ruleInfoDTO)) {\n            throw new BusinessException(\"运算机 ruleInfoDTO 必须非空\")\n        }\n        // 获取规则条件\n        List<RuleConditionDTO> ruleConditionList = ruleInfoDTO.getRuleConditionGroup()\n        if (ruleConditionList == null || ruleConditionList.isEmpty()) {\n            throw new BusinessException(\"运算机 ruleConditionList 必须非空\")\n        }\n        // 将规则条件根据事件编号存储到map中，方便后续操作\n        Map<String, RuleConditionDTO> ruleConditionMapByEventCode = new HashMap<>()\n        for (RuleConditionDTO ruleConditionDTO : ruleConditionList) {\n            ruleConditionMapByEventCode.put(ruleConditionDTO.getEventCode(), ruleConditionDTO)\n        }\n        // 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMapState中\n        updateBigMapWithSmallMap(timestamp)\n        // 清理窗口大小之外的数据\n        cleanupWindowData(timestamp, ruleConditionMapByEventCode)\n        // 判断是否触发规则事件阈值\n        boolean eventResult = evaluateEventThresholds(ruleConditionMapByEventCode, ruleInfoDTO)\n        // 根据规则中事件条件表达式组合判断事件结果 与预警频率 判断否是触发预警\n        if (lastWarningTimeState.value() == null) {\n            lastWarningTimeState.update(0L)\n        }\n        if (eventResult && (timestamp - lastWarningTimeState.value() >= ruleInfoDTO.getWarnInterval())) {\n            lastWarningTimeState.update(timestamp)\n            // 进行预警信息拼接组合\n            String finalWarnMessage = TemplatePlaceholderUtil.replacePlaceholders(\n                    ruleInfoDTO.getWarnMessage(),\n                    ruleInfoDTO,\n                    getLatestEventKafkaDto(),\n                    getProcessorDto()\n            )\n            log.info(\"最终推送的预警信息内容：{}\", finalWarnMessage)\n            out.collect(finalWarnMessage)\n        }\n        // 调试使用，待删除\n        logBigMapState(ruleInfoDTO.getRuleCode(), ruleConditionMapByEventCode.keySet(), null, bigMapState)\n    }\n\n    /**\n     * 判断是否触发规则事件阈值。\n     *\n     * <p>此方法遍历 `bigMapState` 中的所有事件代码及其对应的时间戳和事件值累加，对每个事件代码\n     * 的累加值与预定义的阈值进行比较。如果某个事件代码的累加值超过其阈值，则在结果映射中记录为 `true`。\n     * 最后，根据 `ruleInfoDTO` 中指定的组合条件操作符（如 AND/OR）评估所有事件代码的结果，从而确定\n     * 是否整体满足触发规则的条件。\n     *\n     * @param ruleConditionMapByEventCode 按事件代码分组的规则条件映射，每个事件代码对应一个 `RuleConditionDTO`\n     * @param ruleInfoDTO 规则信息数据传输对象，包含组合条件操作符等规则配置\n     * @return 如果根据组合条件操作符评估后满足规则阈值条件，返回 `true`；否则返回 `false`\n     * @throws Exception 在评估过程中发生任何异常时抛出\n     */\n    private boolean evaluateEventThresholds(Map<String, RuleConditionDTO> ruleConditionMapByEventCode,\n                                            RuleInfoDTO ruleInfoDTO) throws Exception {\n        Map<String, Boolean> eventCodeAndWarnResult = new HashMap<>()\n        for (Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapEntry : bigMapState.entries()) {\n            String eventCode = bigMapEntry.getKey()\n            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()\n            long eventValueSum = timestampAndEventValueKafkaDtoMap.values().stream()\n                    .map(o -> o.f0)\n                    .mapToLong(Long::longValue)\n                    .sum()\n            RuleConditionDTO condition = ruleConditionMapByEventCode.get(eventCode)\n            if (condition != null) {\n                Long eventThreshold = condition.getEventThreshold()\n                eventCodeAndWarnResult.put(eventCode, eventValueSum > eventThreshold)\n            }\n        }\n        boolean eventResult = evaluateEventResults(eventCodeAndWarnResult, ruleInfoDTO.getCombinedConditionOperator())\n        return eventResult\n    }\n\n    /**\n     * 聚合 bigMapState 中的事件值并构建 ProcessorDTO 对象。\n     *\n     * <p>该方法遍历 bigMapState，其中每个键为 eventCode，值为一个包含时间戳和对应\n     * Tuple2<Long, EventKafkaDTO> 的映射。对于每个 eventCode，方法会将所有时间戳\n     * 下的 eventValue（Tuple2 中的第一个值）进行累加，生成一个 eventCode 与其\n     * 累加值的映射。最后，基于这些聚合结果构建并返回一个包含 eventCodeAndValueSumMap 的 ProcessorDTO 对象。</p>\n     *\n     * @return 包含每个 eventCode 对应 eventValue 累加值的 ProcessorDTO 对象\n     * @throws Exception 如果在处理过程中发生错误\n     */\n    private ProcessorDTO getProcessorDto() throws Exception {\n        Map<String, Long> eventCodeAndValueSumMap = new HashMap<>()\n        for (Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapEntry : bigMapState.entries()) {\n            String eventCode = bigMapEntry.getKey()\n            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue()\n            long eventValueSum = timestampAndEventValueKafkaDtoMap.values().stream()\n                    .map(o -> o.f0)\n                    .mapToLong(Long::longValue)\n                    .sum()\n            eventCodeAndValueSumMap.put(eventCode, eventValueSum)\n        }\n        ProcessorDTO processorDTO = ProcessorDTO.builder()\n                .eventCodeAndValueSumMap(eventCodeAndValueSumMap)\n                .build()\n        return processorDTO\n    }\n\n\n    /**\n     * 从所有事件条件累积的值流中检索最新的 Kafka 事件数据。\n     *\n     * <p>此方法遍历 `bigMapState` 中存储的所有事件数据，查找具有最大时间戳的 `EventKafkaDTO` 对象，\n     * 并返回该最新的事件数据。\n     *\n     * @return 最新的 {@link EventKafkaDTO} 对象，如果没有事件数据则返回 {@code null}\n     * @throws Exception 如果在遍历过程中发生异常\n     */\n    private EventKafkaDTO getLatestEventKafkaDto() throws Exception {\n        // 初始化变量，用于存储最新的 EventKafkaDTO 和对应的最大时间戳\n        EventKafkaDTO latestEventKafkaDTO = null;\n        Long maxTimestamp = Long.MIN_VALUE;\n\n        // 遍历 bigMapState 中的每一个大键（eventCode）及其对应的内部映射\n        for (Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapEntry : bigMapState.entries()) {\n            // 获取当前 eventCode 对应的时间戳与事件数据的映射\n            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueKafkaDtoMap = bigMapEntry.getValue();\n\n            // 获取当前映射中的所有条目（时间戳与事件数据对）\n            Set<Map.Entry<Long, Tuple2<Long, EventKafkaDTO>>> entrySet = timestampAndEventValueKafkaDtoMap.entrySet();\n\n            // 遍历当前 eventCode 下的所有时间戳和事件数据对\n            for (Map.Entry<Long, Tuple2<Long, EventKafkaDTO>> entry : entrySet) {\n                Long currentTimestamp = entry.getKey(); // 当前条目的时间戳\n                Tuple2<Long, EventKafkaDTO> value = entry.getValue(); // 包含累加值和事件数据的元组\n\n                // 如果当前时间戳大于已记录的最大时间戳，则更新最大时间戳和最新的事件数据\n                if (currentTimestamp > maxTimestamp) {\n                    maxTimestamp = currentTimestamp;\n                    latestEventKafkaDTO = value.f1; // 获取元组中的 EventKafkaDTO 对象\n                }\n            }\n        }\n\n        // 返回找到的最新的 EventKafkaDTO 对象\n        return latestEventKafkaDTO;\n    }\n\n\n    /**\n     * 将每个事件窗口步长数据集累加的值，添加到窗口大小数据集中bigMapState中\n     */\n    private void updateBigMapWithSmallMap(long timestamp) throws Exception {\n        for (Map.Entry<String, Tuple2<Long, EventKafkaDTO>> smallMapEntry : smallMapState.entries()) {\n            String eventCode = smallMapEntry.getKey()\n            Tuple2<Long, EventKafkaDTO> eventValueAndKafkaDtoTuple2 = smallMapEntry.getValue()\n            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueMap = bigMapState.get(eventCode)\n            if (timestampAndEventValueMap == null || timestampAndEventValueMap.isEmpty()) {\n                timestampAndEventValueMap = new HashMap<>()\n            }\n            timestampAndEventValueMap.put(timestamp, eventValueAndKafkaDtoTuple2)\n            bigMapState.put(eventCode, timestampAndEventValueMap)\n        }\n        // 当前窗口步长的数据已经添加到窗口中了，清空状态\n        smallMapState.clear()\n    }\n\n    /**\n     * 清理窗口大小之外的数据\n     */\n    private void cleanupWindowData(long timestamp, Map<String, RuleConditionDTO> ruleConditionMapByEventCode) throws Exception {\n        for (Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapEntry : bigMapState.entries()) {\n            String eventCode = bigMapEntry.getKey()\n            Map<Long, Tuple2<Long, EventKafkaDTO>> timestampAndEventValueMap = bigMapEntry.getValue()\n            Long windowSize = ruleConditionMapByEventCode.get(eventCode).getWindowSize()\n            long windowThresholdTime = timestamp - windowSize\n            Iterator<Map.Entry<Long, Tuple2<Long, EventKafkaDTO>>> iterator = timestampAndEventValueMap.entrySet().iterator()\n            while (iterator.hasNext()) {\n                Map.Entry<Long, Tuple2<Long, EventKafkaDTO>> next = iterator.next()\n                Long time = next.getKey()\n                if (time <= windowThresholdTime) {\n                    iterator.remove()\n                }\n            }\n            bigMapState.put(eventCode, timestampAndEventValueMap)\n        }\n    }\n\n    private void logBigMapState(String ruleCode, Set<String> eventCodeList, String keyCode, MapState<String,\n            Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMapState) throws Exception {\n        Map<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> bigMap = new HashMap<>()\n        Iterator<Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>>> iterator = bigMapState.iterator()\n        while (iterator.hasNext()) {\n            Map.Entry<String, Map<Long, Tuple2<Long, EventKafkaDTO>>> next = iterator.next()\n            bigMap.put(next.getKey(), next.getValue())\n        }\n        log.warn(\"ProcessorOne对象onTimer方法结束 ruleCode={}, eventCodeList={}, keyCode={}, bigMapState={}\",\n                ruleCode, JsonUtil.toJsonString(eventCodeList), keyCode, JsonUtil.toJsonString(bigMap))\n    }\n\n    /**\n     * 评估事件结果，根据给定的条件操作符返回最终结果。\n     *\n     * @param eventCodeAndWarnResult 包含事件代码及其对应的警告结果的映射\n     * @param conditionOperator 条件操作符，支持 AND 和 OR\n     * @return 根据条件操作符计算后的最终结果（true 或 false）\n     */\n    boolean evaluateEventResults(Map<String, Boolean> eventCodeAndWarnResult, Integer conditionOperator) {\n        if (eventCodeAndWarnResult == null || eventCodeAndWarnResult.isEmpty()) {\n            return false\n        }\n        if (eventCodeAndWarnResult.values() == null || eventCodeAndWarnResult.values().isEmpty()) {\n            return false\n        }\n        // 初始化结果变量，根据条件操作符判断初始值\n        boolean result = conditionOperator == RuleConditionOperatorTypeEnum.AND.getCode()\n\n        // 遍历事件结果的 Map\n        for (Boolean eventResult : eventCodeAndWarnResult.values()) {\n            if (conditionOperator == RuleConditionOperatorTypeEnum.AND.getCode()) {\n                // 对于 AND，只有当所有结果都为 true 时，结果才为 true\n                result = eventResult\n                // 提前结束循环，如果结果已经为 false\n                if (!result) {\n                    break\n                }\n            } else if (conditionOperator == RuleConditionOperatorTypeEnum.OR.getCode()) {\n                // 对于 OR，只要有一个结果为 true，结果就为 true\n                result = eventResult\n                // 提前结束循环，如果结果已经为 true\n                if (result) {\n                    break\n                }\n            }\n        }\n        // 返回最终的评估结果\n        return result\n    }\n}\n', 0, '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', false);

-- ----------------------------
-- Table structure for slr_rule_online_count
-- ----------------------------
DROP TABLE IF EXISTS `slr_rule_online_count`;
CREATE TABLE `slr_rule_online_count`
(
    `id`           bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `online_count` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '规则在线数量',
    `creator`      varchar(255)    NOT NULL DEFAULT '' COMMENT '创建用户',
    `updater`      varchar(255)    NOT NULL DEFAULT '' COMMENT '更新用户',
    `create_time`  datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `update_time`  datetime        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `deleted`      bit(1)          NOT NULL DEFAULT b'0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '星链风控规则在线数量'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of slr_rule_online_count
-- ----------------------------
INSERT INTO `slr_rule_online_count`
VALUES (1, 1, '', '', '1970-01-01 00:00:00', '1970-01-01 00:00:00', b'0');

-- ----------------------------
-- Table structure for sys_bank_database_version
-- ----------------------------
DROP TABLE IF EXISTS `sys_bank_database_version`;
CREATE TABLE `sys_bank_database_version`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `version`       varchar(255) NOT NULL DEFAULT '' COMMENT '卡系统版本',
    `database`      varchar(255) NOT NULL DEFAULT '' COMMENT '生产库名',
    `name`          varchar(255) NOT NULL DEFAULT '' COMMENT '银行名称',
    `bank`          varchar(255) NOT NULL DEFAULT '' COMMENT '银行号',
    `institution`   varchar(255) NOT NULL DEFAULT '' COMMENT '银行机构号',
    `clm_database`  varchar(255) NOT NULL DEFAULT '' COMMENT '积分系统数据库',
    `bank_province` varchar(255) NOT NULL DEFAULT '' COMMENT '银行所属省份地区',
    `bank_type`     varchar(1)   NOT NULL DEFAULT '1' COMMENT '银行类型：0-全国性银行；区域性银行-1（默认）',
    `initals`       varchar(255) NOT NULL DEFAULT '' COMMENT '银行首字母',
    `is_deleted`    varchar(1)   NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-正常，1-删除',
    `create_user`   varchar(255) NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`   datetime     NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '创建时间',
    `update_user`   varchar(255) NOT NULL DEFAULT '' COMMENT '修改人',
    `update_time`   datetime     NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 11
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '银行信息表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_bank_database_version
-- ----------------------------
INSERT INTO `sys_bank_database_version`
VALUES (1, '1.0.0', 'prod_db_1', '中国银行', 'C0001', 'B001', 'clm_db_1', '北京', '0', 'Z', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');
INSERT INTO `sys_bank_database_version`
VALUES (2, '1.0.1', 'prod_db_2', '建设银行', 'C0002', 'B002', 'clm_db_2', '上海', '0', 'J', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');
INSERT INTO `sys_bank_database_version`
VALUES (3, '1.0.2', 'prod_db_3', '工商银行', 'C0003', 'B003', 'clm_db_3', '广东', '1', 'G', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');
INSERT INTO `sys_bank_database_version`
VALUES (4, '1.0.3', 'prod_db_4', '农业银行', 'C0004', 'B004', 'clm_db_4', '江苏', '0', 'N', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');
INSERT INTO `sys_bank_database_version`
VALUES (5, '1.0.4', 'prod_db_5', '交通银行', 'C0005', 'B005', 'clm_db_5', '浙江', '0', 'J', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');
INSERT INTO `sys_bank_database_version`
VALUES (6, '1.0.5', 'prod_db_6', '邮储银行', 'C0006', 'B006', 'clm_db_6', '山东', '1', 'Y', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');
INSERT INTO `sys_bank_database_version`
VALUES (7, '1.0.6', 'prod_db_7', '光大银行', 'C0007', 'B007', 'clm_db_7', '湖北', '0', 'G', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');
INSERT INTO `sys_bank_database_version`
VALUES (8, '1.0.7', 'prod_db_8', '华夏银行', 'C0008', 'B008', 'clm_db_8', '河南', '0', 'H', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');
INSERT INTO `sys_bank_database_version`
VALUES (9, '1.0.8', 'prod_db_9', '民生银行', 'C0009', 'B009', 'clm_db_9', '四川', '1', 'M', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');
INSERT INTO `sys_bank_database_version`
VALUES (10, '1.0.9', 'prod_db_10', '兴业银行', 'C0010', 'B010', 'clm_db_10', '福建', '0', 'X', '0', 'admin',
        '2024-08-22 14:37:51', 'admin', '2024-08-22 14:37:51');

SET FOREIGN_KEY_CHECKS = 1;
