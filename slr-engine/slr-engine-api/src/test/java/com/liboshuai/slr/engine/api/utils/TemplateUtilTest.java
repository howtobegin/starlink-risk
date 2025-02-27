package com.liboshuai.slr.engine.api.utils;

import com.liboshuai.slr.engine.api.dto.FlinkEventDTO;
import com.liboshuai.slr.engine.api.dto.ProcessorDTO;
import com.liboshuai.slr.engine.api.dto.RuleCondDTO;
import com.liboshuai.slr.engine.api.dto.RuleInfoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TemplateUtilTest {

    private RuleInfoDTO ruleInfoDTO;
    private FlinkEventDTO flinkEventDTO;
    private ProcessorDTO processorDTO;

    @BeforeEach
    public void setUp() {
        // 设置RuleInfoDTO
        List<RuleCondDTO> ruleCondGroup = new ArrayList<>();
        RuleCondDTO ruleCondDTO = new RuleCondDTO();
        ruleCondDTO.setWindowValue(10L);
        ruleCondDTO.setWindowUnit("分钟");
        ruleCondDTO.setThreshold(20L);
        ruleCondDTO.setEventField("lottery");
        ruleCondGroup.add(ruleCondDTO);

        ruleInfoDTO = RuleInfoDTO.builder()
                .channel("GAME")
                .ruleCode(1553673459123456000L)
                .ruleName("游戏高频抽奖")
                .ruleDesc("游戏高频抽奖规则")
                .ruleStatus("ENABLED")
                .ruleVersion(1L)
                .alertIntervalValue(30L)
                .alertIntervalUnit("MINUTE")
                .alertProjectNo("GAME_ALERT")
                .alertLevel("HIGH")
                .alertTemplate("[异常高频抽奖]${FlinkEventDTO.eventAttrMap.bankName}: " +
                        "${FlinkEventDTO.eventAttrMap.campaignName}(${FlinkEventDTO.eventAttrMap.campaignId})中" +
                        "游戏用户(${FlinkEventDTO.targetValue})最近${RuleInfoDTO.ruleCondGroup.0.windowValue}" +
                        "${RuleInfoDTO.ruleCondGroup.0.windowUnit}内抽奖数量为${ProcessorDTO.eventValueGroup.lottery}，" +
                        "超过${RuleInfoDTO.ruleCondGroup.0.threshold}次，请您及时查看原因！")
                .targetCode("game_userId")
                .targetField("userId")
                .targetName("用户id")
                .modelCode(1001L)
                .ruleCondCombOp("AND")
                .ruleCondGroup(ruleCondGroup)
                .build();

        // 设置FlinkEventDTO
        Map<String, String> eventAttrMap = new HashMap<>();
        eventAttrMap.put("bankName", "邮储银行");
        eventAttrMap.put("campaignName", "抽奖活动");
        eventAttrMap.put("campaignId", "C0000001");

        flinkEventDTO = FlinkEventDTO.builder()
                .eventTime(1736732339769L)
                .channel("GAME")
                .targetField("userId")
                .targetValue("U000001")
                .eventField("lottery")
                .eventValue("1")
                .eventAttrMap(eventAttrMap)
                .build();

        // 设置ProcessorDTO
        Map<String, Long> eventValueGroup = new HashMap<>();
        eventValueGroup.put("lottery", 21L);

        processorDTO = ProcessorDTO.builder()
                .eventValueGroup(eventValueGroup)
                .build();
    }

    @Test
    public void testReplacePlaceholdersCompleteTemplate() {
        // 测试完整模板替换
        String template = ruleInfoDTO.getAlertTemplate();
        String expected = "[异常高频抽奖]邮储银行: 抽奖活动(C0000001)中游戏用户(U000001)最近10分钟内抽奖数量为21，超过20次，请您及时查看原因！";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO, flinkEventDTO, processorDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersSimpleFields() {
        // 测试简单字段替换
        String template = "规则名称: ${RuleInfoDTO.ruleName}, 目标用户: ${FlinkEventDTO.targetValue}";
        String expected = "规则名称: 游戏高频抽奖, 目标用户: U000001";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO, flinkEventDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersMapAccess() {
        // 测试Map访问
        String template = "银行: ${FlinkEventDTO.eventAttrMap.bankName}, 活动ID: ${FlinkEventDTO.eventAttrMap.campaignId}";
        String expected = "银行: 邮储银行, 活动ID: C0000001";

        String result = TemplateUtil.replacePlaceholders(template, flinkEventDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersListIndexAccess() {
        // 测试列表索引访问
        String template = "窗口值: ${RuleInfoDTO.ruleCondGroup.0.windowValue}, 窗口单位: ${RuleInfoDTO.ruleCondGroup.0.windowUnit}";
        String expected = "窗口值: 10, 窗口单位: 分钟";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersNestedMapValues() {
        // 测试嵌套的Map值
        String template = "抽奖次数: ${ProcessorDTO.eventValueGroup.lottery}";
        String expected = "抽奖次数: 21";

        String result = TemplateUtil.replacePlaceholders(template, processorDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersNonExistingClass() {
        // 测试不存在的类
        String template = "${NonExistingClass.field}";
        String expected = "${NonExistingClass.field}";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO, flinkEventDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersNonExistingField() {
        // 测试不存在的字段
        String template = "${RuleInfoDTO.nonExistingField}";
        String expected = "${RuleInfoDTO.nonExistingField}";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersNonExistingMapKey() {
        // 测试不存在的Map键
        String template = "${FlinkEventDTO.eventAttrMap.nonExistingKey}";
        String expected = "${FlinkEventDTO.eventAttrMap.nonExistingKey}";

        String result = TemplateUtil.replacePlaceholders(template, flinkEventDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersOutOfBoundListIndex() {
        // 修改规则条件组大小以测试越界索引
        List<RuleCondDTO> singleItemList = new ArrayList<>();
        singleItemList.add(new RuleCondDTO());
        ruleInfoDTO.setRuleCondGroup(singleItemList);

        // 测试越界的列表索引
        String template = "${RuleInfoDTO.ruleCondGroup.5.windowValue}";
        String expected = "${RuleInfoDTO.ruleCondGroup.5.windowValue}";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersNullObject() {
        // 测试空对象
        String template = "测试: ${RuleInfoDTO.ruleName}";
        String expected = "测试: ${RuleInfoDTO.ruleName}";

        String result = TemplateUtil.replacePlaceholders(template, (Object[]) null);
        assertEquals(template, result);
    }

    @Test
    public void testReplacePlaceholdersEmptyTemplate() {
        // 测试空模板
        String result = TemplateUtil.replacePlaceholders("", ruleInfoDTO, flinkEventDTO);
        assertEquals("", result);

        result = TemplateUtil.replacePlaceholders(null, ruleInfoDTO, flinkEventDTO);
        assertNull(result);
    }

    @Test
    public void testReplacePlaceholdersNullFieldValue() {
        // 测试空字段值
        ruleInfoDTO.setRuleName(null);
        String template = "规则名称: ${RuleInfoDTO.ruleName}";
        String expected = "规则名称: ${RuleInfoDTO.ruleName}";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO);
        assertEquals(expected, result);
    }

    // 这个方法模拟TemplateUtil中的一个扩展功能，用于处理命名对象
    private String customReplacePlaceholders(String template, Map<String, Object> namedObjects) {
        // 实际项目中可以添加这个方法到TemplateUtil类中
        if (template == null || namedObjects == null || namedObjects.isEmpty()) {
            return template;
        }

        // 转换为对象数组调用原方法
        return TemplateUtil.replacePlaceholders(template, namedObjects.values().toArray());
    }

    @Test
    public void testReplacePlaceholdersMixedContent() {
        // 测试混合内容替换
        String template = "这是${RuleInfoDTO.ruleName}的测试，用户${FlinkEventDTO.targetValue}在${FlinkEventDTO.eventAttrMap.bankName}";
        String expected = "这是游戏高频抽奖的测试，用户U000001在邮储银行";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO, flinkEventDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersEmptyMap() {
        // 测试空Map
        flinkEventDTO.setEventAttrMap(new HashMap<>());
        String template = "${FlinkEventDTO.eventAttrMap.bankName}";
        String expected = "${FlinkEventDTO.eventAttrMap.bankName}";

        String result = TemplateUtil.replacePlaceholders(template, flinkEventDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersEmptyList() {
        // 测试空List
        ruleInfoDTO.setRuleCondGroup(new ArrayList<>());
        String template = "${RuleInfoDTO.ruleCondGroup.0.windowValue}";
        String expected = "${RuleInfoDTO.ruleCondGroup.0.windowValue}";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO);
        assertEquals(expected, result);
    }

    @Test
    public void testReplacePlaceholdersInvalidPlaceholder() {
        // 测试无效的占位符
        String template = "${.invalid}";
        String expected = "${.invalid}";

        String result = TemplateUtil.replacePlaceholders(template, ruleInfoDTO);
        assertEquals(expected, result);
    }
}