package com.liboshuai.starlink;

import com.liboshuai.slr.module.engine.dto.KafkaEventDTO;
import com.liboshuai.slr.module.engine.dto.ProcessorDTO;
import com.liboshuai.slr.module.engine.dto.RuleCondDTO;
import com.liboshuai.slr.module.engine.dto.RuleInfoDTO;
import com.liboshuai.slr.module.engine.utils.TemplateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.*;

public class TemplateUtilTest {

    @Test
    public void testReplacePlaceholders() {
        RuleCondDTO ruleCondDTO = RuleCondDTO.builder()
                .ruleCode("ruleCode1")
                .eventCode("eventCode1")
                .threshold(10L)
                .condType("CYCLE")
                .windowValue(1L)
                .windowUnit("MINUTE")
                .crossHistory(true)
                .build();
        RuleCondDTO ruleCondDTO2 = RuleCondDTO.builder()
                .ruleCode("ruleCode2")
                .eventCode("eventCode2")
                .threshold(20L)
                .condType("CYCLE")
                .windowValue(2L)
                .windowUnit("MINUTE")
                .crossHistory(true)
                .build();
        RuleInfoDTO ruleInfoDTO = RuleInfoDTO.builder()
                .ruleCode("ruleCode1")
                .ruleName("ruleName1")
                .ruleDesc("ruleDesc1")
                .modelCode("modelCode1")
                .ruleCondCombOp("AND")
                .alertMessage("[异常高频抽奖]：${KafkaEventDTO.eventAttribute.campaignId}(${KafkaEventDTO.eventAttribute.campaignName})中游戏用户(${KafkaEventDTO.keyValue})最近${RuleInfoDTO.ruleCondGroup.0.windowValue}内抽奖数量为${ProcessorDTO.eventCodeAndValueSumMap.GAME_LOTTERY}，超过${RuleInfoDTO.ruleCondGroup.0.threshold}次，请您及时查看原因！")
                .alertIntervalUnit("MINUTE")
                .alertIntervalValue(10L)
                .ruleStatus("ONLINE")
                .ruleCondGroup(Arrays.asList(ruleCondDTO, ruleCondDTO2))
                .build();

        Map<String, String> eventAttribute = new HashMap<>();
        eventAttribute.put("campaignId", "c000001");
        eventAttribute.put("campaignName", "活动01");
        KafkaEventDTO kafkaEventDTO = KafkaEventDTO.builder()
                .channel("game")
                .targetField("keyCode1")
                .targetValue("keyValue1")
                .eventField("eventCode1")
                .eventValue("eventValue1")
                .eventAttrMap(eventAttribute)
                .eventTime(System.currentTimeMillis())
                .build();
        Map<String, Long> eventCodeAndValueSumMap = new HashMap<>();
        eventCodeAndValueSumMap.put("GAME_LOTTERY", 10L);
        ProcessorDTO processorDTO = ProcessorDTO.builder()
                .eventFieldAndValueSumMap(eventCodeAndValueSumMap)
                .build();

        // 使用工具类替换模板中的占位符
        String finalWarnMessage = TemplateUtil.replacePlaceholders(
                ruleInfoDTO.getAlertMessage(),
                ruleInfoDTO,
                kafkaEventDTO,
                processorDTO
        );

        System.out.println("finalWarnMessage: " + finalWarnMessage);
    }

    @Test
    public void testReplacePlaceholders2() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("userId", "userId01");
        attributes.put("bankName", "bankName01");
        attributes.put("campaignId", "campaignId01");
        attributes.put("campaignName", "campaignName01");

        KafkaEventDTO kafkaEventDTO = KafkaEventDTO.builder()
                .channel("game")
                .targetField("keyCode1")
                .targetValue("keyValue1")
                .eventField("eventCode1")
                .eventValue("eventValue1")
                .eventAttrMap(attributes)
                .eventTime(System.currentTimeMillis())
                .build();

        List<RuleCondDTO> RuleCondDTOS = new ArrayList<>();
        RuleCondDTOS.add(RuleCondDTO.builder()
                .ruleCode("ruleCode1")
                .eventCode("eventCode1")
                .threshold(10L)
                .condType("CYCLE")
                .windowValue(20L)
                .windowUnit("MINUTE")
                .crossHistory(true)
                .build());
        RuleCondDTOS.add(RuleCondDTO.builder()
                .ruleCode("ruleCode2")
                .eventCode("eventCode2")
                .threshold(20L)
                .condType("CYCLE")
                .windowValue(20L)
                .windowUnit("MINUTE")
                .crossHistory(true)
                .build());
        RuleInfoDTO ruleInfoDTO = RuleInfoDTO.builder()
                .channel("game")
                .ruleCode("ruleCode1")
                .ruleName("ruleName1")
                .ruleDesc("ruleDesc1")
                .modelCode("modelCode1")
                .ruleCondCombOp("AND")
                .alertMessage("warnMessage1")
                .alertIntervalUnit("MINUTE")
                .alertIntervalValue(10L)
                .ruleStatus("ONLINE")
                .ruleCondGroup(RuleCondDTOS)
                .modelCode("xxx")
                .build();

        String messageTemplate = "[异常高频抽奖]${EventKafkaDTO.attribute.bankName}：" +
                "${EventKafkaDTO.attribute.campaignName}(${EventKafkaDTO.attribute.campaignId})" +
                "中游戏用户(${EventKafkaDTO.attribute.userId})" +
                "最近${RuleInfoDTO.ruleConditionGroup.1.windowSize}内抽奖数量为${xxx}，" +
                "超过${RuleInfoDTO.ruleConditionGroup.1.eventThreshold}次，请您及时查看原因！";

        // 使用工具类替换模板中的占位符
        String result = TemplateUtil.replacePlaceholders(messageTemplate, kafkaEventDTO, ruleInfoDTO);

        System.out.println("Result: " + result);
    }
}

@Builder
@NoArgsConstructor
@AllArgsConstructor
class KafkaPojo {
    private String channel;
    private String userName;
    private String userId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MysqlPojo {
    private String ruleName;
    private String ruleCode;
    private RulePojo rulePojo;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RulePojo {
    private String ruleValue;
    private String ruleType;
}