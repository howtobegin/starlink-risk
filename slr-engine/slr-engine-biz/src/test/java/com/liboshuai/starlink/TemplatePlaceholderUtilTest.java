package com.liboshuai.starlink;

import com.liboshuai.starlink.slr.engine.api.dto.KafkaEventDTO;
import com.liboshuai.starlink.slr.engine.api.dto.RuleCondDTO;
import com.liboshuai.starlink.slr.engine.api.dto.RuleInfoDTO;
import com.liboshuai.starlink.slr.engine.api.util.TemplatePlaceholderUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplatePlaceholderUtilTest {

    @Test
    public void testReplacePlaceholders() {
        KafkaPojo kafkaPojo = KafkaPojo.builder()
                .channel("game")
                .userName("wdy")
                .userId("000001")
                .build();

        MysqlPojo mysqlPojo = MysqlPojo.builder()
                .ruleName("游戏高频抽奖")
                .ruleCode("game_lottery")
                .rulePojo(RulePojo.builder().ruleType("1").ruleValue("10").build())
                .build();

        String messageTemplate = "${KafkaPojo.channel}渠道的${KafkaPojo.userName}(${KafkaPojo.userId})用户触发了" +
                "${MysqlPojo.ruleName}(${MysqlPojo.ruleCode})规则，其中规则类型为${MysqlPojo.rulePojo.ruleType}" +
                "、规则值为${MysqlPojo.rulePojo.ruleValue}。请尽快处理!";

        String messageTemplate2 = "[异常高频抽奖]${EventKafkaDTO.attribute.bankName}：${EventKafkaDTO.attribute.campaignName}" +
                "(${EventKafkaDTO.attribute.campaignId})中游戏用户(${EventKafkaDTO.attribute.userId})" +
                "最近${RuleInfoDTO.ruleConditionGroup.0.windowSize}内抽奖数量为${xxx}，" +
                "超过${RuleInfoDTO.ruleConditionGroup.0.eventThreshold}次，请您及时查看原因！";

        // 使用工具类替换模板中的占位符
        String result = TemplatePlaceholderUtil.replacePlaceholders(messageTemplate, kafkaPojo, mysqlPojo);

        System.out.println("Result: " + result);
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
                .keyCode("keyCode1")
                .keyValue("keyValue1")
                .eventCode("eventCode1")
                .eventValue("eventValue1")
                .eventAttribute(attributes)
                .timestamp(System.currentTimeMillis())
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
        String result = TemplatePlaceholderUtil.replacePlaceholders(messageTemplate, kafkaEventDTO, ruleInfoDTO);

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