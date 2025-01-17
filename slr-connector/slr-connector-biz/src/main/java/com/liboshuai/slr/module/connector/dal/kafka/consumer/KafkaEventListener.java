package com.liboshuai.slr.module.connector.dal.kafka.consumer;

import com.liboshuai.slr.framework.common.util.json.JsonUtils;
import com.liboshuai.slr.module.admin.api.riskRule.RuleInfoApi;
import com.liboshuai.slr.module.admin.api.riskRule.dto.RuleInfoApiDTO;
import com.liboshuai.slr.module.connector.api.alertMessage.dto.AlertMessageApiDTO;
import com.liboshuai.slr.module.connector.convert.alertMessage.AlertMessageConvert;
import com.liboshuai.slr.module.connector.dal.dataobject.alertMessage.AlertMessageDO;
import com.liboshuai.slr.module.connector.dal.mongo.AlertMessageRepository;
import com.liboshuai.slr.module.connector.rest.rsoAlarm.RsoAlarmRestApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener {

    private final RuleInfoApi ruleInfoApi;
    private final RsoAlarmRestApi rsoAlarmRestApi;
    private final AlertMessageConvert alertMessageConvert;
    private final AlertMessageRepository alertMessageRepository;

    @KafkaListener(
            topics = "${slr-connector.kafka.consumer_topic}",
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
            // 将预警信息异步推送给微信预警平台
            RuleInfoApiDTO ruleInfoDTO = ruleInfoApi.getCacheRuleInfo(alertMessageApiDTO.getRuleCode());
            // FIXME: 测试时，临时注释
//            rsoAlarmRestApi.sendMsgToRso(
//                    ruleInfoDTO.getAlertProjectNo(),
//                    ruleInfoDTO.getAlertLevel(),
//                    LocalDateTimeUtils.convertLocalDateTime2Str(alertMessageApiDTO.getAlertTime()),
//                    alertMessageApiDTO.getAlertMessage()
//            );
        }

        if (!validAlertMessageApiDTOList.isEmpty()) {
            // 将预警消息批量保存到 MongoDB
            List<AlertMessageDO> alertMessageDOList = alertMessageConvert.batchConvertDto2Mongo(validAlertMessageApiDTOList);
            alertMessageRepository.saveAll(alertMessageDOList);
        }
        // 手动提交偏移量
        ack.acknowledge();
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
