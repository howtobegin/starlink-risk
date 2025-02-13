package com.liboshuai.slr.server.biz.dal.kafka.consumer;

import com.liboshuai.slr.engine.api.dto.AlertDTO;
import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.RuleInfoDTO;
import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.framework.common.util.string.TemplateUtil;
import com.liboshuai.slr.server.biz.convert.alert.AlertConvert;
import com.liboshuai.slr.server.biz.dal.dataobject.alert.MongoAlertDO;
import com.liboshuai.slr.server.biz.dal.mongo.alert.AlertRepository;
import com.liboshuai.slr.server.biz.rest.client.rsoAlarm.RsoAlarmClient;
import com.liboshuai.slr.server.biz.service.kafkaEvent.EventService;
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
public class AlertListener {

    private final RuleInfoService ruleInfoService;
    private final RsoAlarmClient rsoAlarmClient;
    private final AlertConvert alertConvert;
    private final AlertRepository alertRepository;
    private final EventService eventService;

    @KafkaListener(
            topics = "${slr-server.kafka.alert.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            errorHandler = "kafkaConsumerExceptionHandler"
    )
    public void flinkAlertMessage(List<ConsumerRecord<String, String>> consumerRecordList, Acknowledgment ack) {
        List<AlertDTO> validAlertDTOList = new ArrayList<>();

        for (ConsumerRecord<String, String> record : consumerRecordList) {
            String recordValue = record.value();
//            log.info("消费到预警消息：{}", recordValue);
            AlertDTO alertDTO = JsonUtils.parseObject(recordValue, AlertDTO.class);
            if (alertDTO == null) {
                log.warn("无效的 AlertMessageDTO 数据：解析结果为空！原始数据：{}", recordValue);
                continue;
            }
            // 效验 AlertMessageDTO 是否包含所有必要字段
            ValidationResult validationResult = validateAlertMessageDTO(alertDTO);
            if (!validationResult.isValid()) {
                log.warn("无效的 AlertMessageDTO 数据：缺少必要字段 [{}]！数据内容：{}",
                        String.join(" ", validationResult.getMissingFields()), alertDTO);
                continue;
            }
            validAlertDTOList.add(alertDTO);
        }
        List<AlertDTO> finalAlertDtoList = new ArrayList<>();
        if (!validAlertDTOList.isEmpty()) {
            // 从mongo中获取事件id与其对应的数据数据
            Map<String, FlinkEventDTO> eventIdAndKafkaEventMap = findEventIdAndKafkaEventMap(validAlertDTOList);
            // 遍历预警信息，补充事件数据并推送到微信预警平台
            for (AlertDTO alertDTO : validAlertDTOList) {
                // 根据mongo中的事件数据补充预警信息
                String alertMessage = alertDTO.getMessage();
                String eventId = alertDTO.getEventId();
                FlinkEventDTO flinkEventDTO = eventIdAndKafkaEventMap.get(eventId);
                if (Objects.isNull(flinkEventDTO)) {
                    flinkEventDTO = new FlinkEventDTO();
                }
                alertMessage = TemplateUtil.replacePlaceholders(alertMessage, flinkEventDTO);
                // 将预警信息异步推送给微信预警平台
                RuleInfoDTO ruleInfoDTO = ruleInfoService.getCacheRuleInfo(alertDTO.getRuleCode());
                // FIXME: 测试时，临时注释
//                rsoAlarmClient.sendMsgToRso(
//                        ruleInfoDTO.getAlertProjectNo(),
//                        ruleInfoDTO.getAlertLevel(),
//                        LocalDateTimeUtils.convertLocalDateTime2Str(alertMessageDTO.getAlertTime()),
//                        alertMessage
//                );
                // 添加到 finalAlertMessageDtoList 中，并补充字段数据
                alertDTO.setMessage(alertMessage);
                alertDTO.setTargetValue(flinkEventDTO.getTargetValue());
                finalAlertDtoList.add(alertDTO);
            }
            // 将预警消息批量保存到 MongoDB
            List<MongoAlertDO> mongoAlertDOList = alertConvert.batchConvertDto2Mongo(finalAlertDtoList);
            alertRepository.saveAll(mongoAlertDOList);
        }
        // 手动提交偏移量
        ack.acknowledge();
    }

    /**
     * 从mongo中获取事件id与其对应的数据数据
     */
    private Map<String, FlinkEventDTO> findEventIdAndKafkaEventMap(List<AlertDTO> validAlertDTOList) {
        Map<String, FlinkEventDTO> eventIdAndKafkaEventMap = new HashMap<>();
        List<String> eventIdList = validAlertDTOList.stream().map(AlertDTO::getEventId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(eventIdList)) {
            List<FlinkEventDTO> flinkEventDTOList = eventService.selectListByEventIds(eventIdList);
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
    private ValidationResult validateAlertMessageDTO(AlertDTO dto) {
        List<String> missingFields = new ArrayList<>();

        if (!StringUtils.hasText(dto.getChannel())) {
            missingFields.add("channel");
        }
        if (Objects.isNull(dto.getRuleCode())) {
            missingFields.add("ruleCode");
        }
        if (!StringUtils.hasText(dto.getMessage())) {
            missingFields.add("alertMessage");
        }
        if (Objects.isNull(dto.getTime())) {
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
