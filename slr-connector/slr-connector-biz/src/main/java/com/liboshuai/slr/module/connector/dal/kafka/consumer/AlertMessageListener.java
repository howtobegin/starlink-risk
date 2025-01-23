package com.liboshuai.slr.module.connector.dal.kafka.consumer;

import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.string.TemplateUtil;
import com.liboshuai.slr.module.admin.api.riskRule.RuleInfoApi;
import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleInfoApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageApiDTO;
import com.liboshuai.slr.module.connector.convert.alertMessage.AlertMessageConvert;
import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.AlertMessageDO;
import com.liboshuai.slr.module.connector.dal.mongo.AlertMessageRepository;
import com.liboshuai.slr.module.connector.rest.rsoAlarm.RsoAlarmRestApi;
import com.liboshuai.slr.module.connector.service.kafkaEvent.KafkaEventService;
import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
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

    private final RuleInfoApi ruleInfoApi;
    private final RsoAlarmRestApi rsoAlarmRestApi;
    private final AlertMessageConvert alertMessageConvert;
    private final AlertMessageRepository alertMessageRepository;
    private final KafkaEventService kafkaEventService;

    @KafkaListener(
            topics = "${slr-connector.kafka.consumer.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            errorHandler = "kafkaConsumerExceptionHandler"
    )
    public void flinkAlertMessage(List<ConsumerRecord<String, String>> consumerRecordList, Acknowledgment ack) {
        List<AlertMessageApiDTO> validAlertMessageApiDTOList = new ArrayList<>();

        for (ConsumerRecord<String, String> record : consumerRecordList) {
            String recordValue = record.value();
            log.info("消费到预警消息：{}", recordValue);
            AlertMessageApiDTO alertMessageApiDTO = JsonUtils.parseObject(recordValue, AlertMessageApiDTO.class);
            if (alertMessageApiDTO == null) {
                log.warn("无效的 AlertMessageDTO 数据：解析结果为空！原始数据：{}", recordValue);
                continue;
            }
            // 效验 AlertMessageDTO 是否包含所有必要字段
            ValidationResult validationResult = validateAlertMessageDTO(alertMessageApiDTO);
            if (!validationResult.isValid()) {
                log.warn("无效的 AlertMessageDTO 数据：缺少必要字段 [{}]！数据内容：{}",
                        String.join(" ", validationResult.getMissingFields()), alertMessageApiDTO);
                continue;
            }
            validAlertMessageApiDTOList.add(alertMessageApiDTO);
        }
        List<AlertMessageApiDTO> finalAlertMessageApiDtoList = new ArrayList<>();
        if (!validAlertMessageApiDTOList.isEmpty()) {
            // 从mongo中获取事件id与其对应的数据数据
            Map<Long, KafkaEventDTO> eventIdAndKafkaEventMap = findEventIdAndKafkaEventMap(validAlertMessageApiDTOList);
            // 遍历预警信息，补充事件数据并推送到微信预警平台
            for (AlertMessageApiDTO alertMessageApiDTO : validAlertMessageApiDTOList) {
                // 根据mongo中的事件数据补充预警信息
                String alertMessage = alertMessageApiDTO.getAlertMessage();
                Long eventId = alertMessageApiDTO.getEventId();
                KafkaEventDTO kafkaEventDTO = eventIdAndKafkaEventMap.get(eventId);
                if (Objects.isNull(kafkaEventDTO)) {
                    kafkaEventDTO = new KafkaEventDTO();
                }
                alertMessage = TemplateUtil.replacePlaceholders(alertMessage, kafkaEventDTO);
                // 将预警信息异步推送给微信预警平台
                RuleInfoApiDTO ruleInfoDTO = ruleInfoApi.getCacheRuleInfo(alertMessageApiDTO.getRuleCode());
                // FIXME: 测试时，临时注释
//                rsoAlarmRestApi.sendMsgToRso(
//                        ruleInfoDTO.getAlertProjectNo(),
//                        ruleInfoDTO.getAlertLevel(),
//                        LocalDateTimeUtils.convertLocalDateTime2Str(alertMessageApiDTO.getAlertTime()),
//                        alertMessage
//                );
                // 添加到 finalAlertMessageApiDtoList 中，并补充字段数据
                alertMessageApiDTO.setAlertMessage(alertMessage);
                alertMessageApiDTO.setTargetValue(kafkaEventDTO.getTargetValue());
                finalAlertMessageApiDtoList.add(alertMessageApiDTO);
            }
            // 将预警消息批量保存到 MongoDB
            List<AlertMessageDO> alertMessageDOList = alertMessageConvert.batchConvertDto2Mongo(finalAlertMessageApiDtoList);
            alertMessageRepository.saveAll(alertMessageDOList);
        }
        // 手动提交偏移量
        ack.acknowledge();
    }

    /**
     * 从mongo中获取事件id与其对应的数据数据
     */
    private Map<Long, KafkaEventDTO> findEventIdAndKafkaEventMap(List<AlertMessageApiDTO> validAlertMessageApiDTOList) {
        Map<Long, KafkaEventDTO> eventIdAndKafkaEventMap = new HashMap<>();
        List<Long> eventIdList = validAlertMessageApiDTOList.stream().map(AlertMessageApiDTO::getEventId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(eventIdList)) {
            List<KafkaEventDTO> kafkaEventDTOList = kafkaEventService.selectListByEventIds(eventIdList);
            if (!CollectionUtils.isEmpty(kafkaEventDTOList)) {
                eventIdAndKafkaEventMap = kafkaEventDTOList.stream().collect(Collectors.toMap(
                        KafkaEventDTO::getEventId,
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
    private ValidationResult validateAlertMessageDTO(AlertMessageApiDTO dto) {
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
