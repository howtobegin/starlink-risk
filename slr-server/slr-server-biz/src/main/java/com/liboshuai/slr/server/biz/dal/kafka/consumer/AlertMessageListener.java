package com.liboshuai.slr.server.biz.dal.kafka.consumer;

import com.liboshuai.slr.engine.api.dto.AlertMessageDTO;
import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.RuleInfoDTO;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.string.TemplateUtil;
import com.liboshuai.slr.server.biz.convert.alertMessage.AlertMessageConvert;
import com.liboshuai.slr.server.biz.dal.dataobject.alertMessage.AlertMessageDO;
import com.liboshuai.slr.server.biz.dal.mongo.AlertMessageRepository;
import com.liboshuai.slr.server.biz.rest.client.rsoAlarm.RsoAlarmClient;
import com.liboshuai.slr.server.biz.service.kafkaEvent.KafkaEventService;
import com.liboshuai.slr.server.biz.service.riskRule.RuleInfoService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertMessageListener {

    private final RuleInfoService ruleInfoService;
    private final RsoAlarmClient rsoAlarmClient;
    private final AlertMessageConvert alertMessageConvert;
    private final AlertMessageRepository alertMessageRepository;
    private final KafkaEventService kafkaEventService;

    @KafkaListener(
            topics = "${slr-connector.kafka.alert.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            errorHandler = "kafkaConsumerExceptionHandler"
    )
    public void flinkAlertMessage(List<ConsumerRecord<String, String>> consumerRecordList, Acknowledgment ack) {
        List<AlertMessageDTO> validAlertMessageDTOList = new ArrayList<>();

        for (ConsumerRecord<String, String> record : consumerRecordList) {
            String recordValue = record.value();
//            log.info("消费到预警消息：{}", recordValue);
            AlertMessageDTO alertMessageDTO = JsonUtils.parseObject(recordValue, AlertMessageDTO.class);
            if (alertMessageDTO == null) {
                log.warn("无效的 AlertMessageDTO 数据：解析结果为空！原始数据：{}", recordValue);
                continue;
            }
            // 效验 AlertMessageDTO 是否包含所有必要字段
            ValidationResult validationResult = validateAlertMessageDTO(alertMessageDTO);
            if (!validationResult.isValid()) {
                log.warn("无效的 AlertMessageDTO 数据：缺少必要字段 [{}]！数据内容：{}",
                        String.join(" ", validationResult.getMissingFields()), alertMessageDTO);
                continue;
            }
            validAlertMessageDTOList.add(alertMessageDTO);
        }
        List<AlertMessageDTO> finalAlertMessageDtoList = new ArrayList<>();
        if (!validAlertMessageDTOList.isEmpty()) {
            // 从mongo中获取事件id与其对应的数据数据
            Map<Long, FlinkEventDTO> eventIdAndKafkaEventMap = findEventIdAndKafkaEventMap(validAlertMessageDTOList);
            // 遍历预警信息，补充事件数据并推送到微信预警平台
            for (AlertMessageDTO alertMessageDTO : validAlertMessageDTOList) {
                // 根据mongo中的事件数据补充预警信息
                String alertMessage = alertMessageDTO.getAlertMessage();
                Long eventId = alertMessageDTO.getEventId();
                FlinkEventDTO flinkEventDTO = eventIdAndKafkaEventMap.get(eventId);
                if (Objects.isNull(flinkEventDTO)) {
                    flinkEventDTO = new FlinkEventDTO();
                }
                alertMessage = TemplateUtil.replacePlaceholders(alertMessage, flinkEventDTO);
                // 将预警信息异步推送给微信预警平台
                RuleInfoDTO ruleInfoDTO = ruleInfoService.getCacheRuleInfo(alertMessageDTO.getRuleCode());
                // FIXME: 测试时，临时注释
//                rsoAlarmClient.sendMsgToRso(
//                        ruleInfoDTO.getAlertProjectNo(),
//                        ruleInfoDTO.getAlertLevel(),
//                        LocalDateTimeUtils.convertLocalDateTime2Str(alertMessageDTO.getAlertTime()),
//                        alertMessage
//                );
                // 添加到 finalAlertMessageDtoList 中，并补充字段数据
                alertMessageDTO.setAlertMessage(alertMessage);
                alertMessageDTO.setTargetValue(flinkEventDTO.getTargetValue());
                finalAlertMessageDtoList.add(alertMessageDTO);
            }
            // 将预警消息批量保存到 MongoDB
            List<AlertMessageDO> alertMessageDOList = alertMessageConvert.batchConvertDto2Mongo(finalAlertMessageDtoList);
            alertMessageRepository.saveAll(alertMessageDOList);
        }
        // 手动提交偏移量
        ack.acknowledge();
    }

    /**
     * 从mongo中获取事件id与其对应的数据数据
     */
    private Map<Long, FlinkEventDTO> findEventIdAndKafkaEventMap(List<AlertMessageDTO> validAlertMessageDTOList) {
        Map<Long, FlinkEventDTO> eventIdAndKafkaEventMap = new HashMap<>();
        List<Long> eventIdList = validAlertMessageDTOList.stream().map(AlertMessageDTO::getEventId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(eventIdList)) {
            List<FlinkEventDTO> flinkEventDTOList = kafkaEventService.selectListByEventIds(eventIdList);
            if (!CollectionUtils.isEmpty(flinkEventDTOList)) {
                eventIdAndKafkaEventMap = flinkEventDTOList.stream().collect(Collectors.toMap(
                        FlinkEventDTO::getEventId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
            }
        }
        return eventIdAndKafkaEventMap;
    }

    /**
     * 验证 AlertMessageDTO 是否包含所有必要字段。
     *
     * @param dto 要验证的 AlertMessageDTO 对象
     * @return ValidationResult 包含验证结果和缺失的字段列表
     */
    private ValidationResult validateAlertMessageDTO(AlertMessageDTO dto) {
        List<String> missingFields = new ArrayList<>();

        if (!StringUtils.hasText(dto.getChannel())) {
            missingFields.add("channel");
        }
        if (Objects.isNull(dto.getRuleCode())) {
            missingFields.add("ruleCode");
        }
        if (!StringUtils.hasText(dto.getAlertMessage())) {
            missingFields.add("alertMessage");
        }
        if (Objects.isNull(dto.getAlertTime())) {
            missingFields.add("alertTime");
        }

        return new ValidationResult(missingFields.isEmpty(), missingFields);
    }

    /**
     * 内部类用于封装验证结果。
     */
    @Getter
    private static class ValidationResult {
        private final boolean valid;
        private final List<String> missingFields;

        public ValidationResult(boolean valid, List<String> missingFields) {
            this.valid = valid;
            this.missingFields = missingFields;
        }

    }
}
