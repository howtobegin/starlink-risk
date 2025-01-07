package com.liboshuai.starlink;

import com.liboshuai.slr.module.engine.processor.impl.ProcessorOne;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventEvaluatorTest {

    private ProcessorOne processorOne;

    @BeforeEach
    void setUp() {
        processorOne = new ProcessorOne();
    }

    @Test
    @DisplayName("测试输入为 null 时返回 false")
    void testEvaluateEventResults_NullInput() {
        boolean result = processorOne.evaluateEventResults(null, "AND");
        assertFalse(result, "当输入为 null 时，结果应为 false");
    }

    @Test
    @DisplayName("测试输入为空 Map 时返回 false")
    void testEvaluateEventResults_EmptyMap() {
        Map<String, Boolean> input = new HashMap<>();
        boolean result = processorOne.evaluateEventResults(input, "OR");
        assertFalse(result, "当输入为空 Map 时，结果应为 false");
    }

    @Test
    @DisplayName("测试单一元素 Map 返回其值 - true")
    void testEvaluateEventResults_SingleTrue() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("event1", true);
        boolean result = processorOne.evaluateEventResults(input, "AND");
        assertTrue(result, "单一 true 元素应返回 true");
    }

    @Test
    @DisplayName("测试单一元素 Map 返回其值 - false")
    void testEvaluateEventResults_SingleFalse() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("event1", false);
        boolean result = processorOne.evaluateEventResults(input, "OR");
        assertFalse(result, "单一 false 元素应返回 false");
    }

    @Test
    @DisplayName("测试多个元素 AND 操作符 - 全为 true")
    void testEvaluateEventResults_MultipleAndAllTrue() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("event1", true);
        input.put("event2", true);
        input.put("event3", true);
        boolean result = processorOne.evaluateEventResults(input, "AND");
        assertTrue(result, "所有元素为 true，且操作符为 AND 时，结果应为 true");
    }

    @Test
    @DisplayName("测试多个元素 AND 操作符 - 存在 false")
    void testEvaluateEventResults_MultipleAndContainsFalse() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("event1", true);
        input.put("event2", false);
        input.put("event3", true);
        boolean result = processorOne.evaluateEventResults(input, "AND");
        assertFalse(result, "存在 false 元素，且操作符为 AND 时，结果应为 false");
    }

    @Test
    @DisplayName("测试多个元素 OR 操作符 - 至少一个 true")
    void testEvaluateEventResults_MultipleOrAtLeastOneTrue() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("event1", false);
        input.put("event2", true);
        input.put("event3", false);
        boolean result = processorOne.evaluateEventResults(input, "OR");
        assertTrue(result, "至少一个 true 元素，且操作符为 OR 时，结果应为 true");
    }

    @Test
    @DisplayName("测试多个元素 OR 操作符 - 全为 false")
    void testEvaluateEventResults_MultipleOrAllFalse() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("event1", false);
        input.put("event2", false);
        input.put("event3", false);
        boolean result = processorOne.evaluateEventResults(input, "OR");
        assertFalse(result, "所有元素为 false，且操作符为 OR 时，结果应为 false");
    }

    @Test
    @DisplayName("测试无效的操作符抛出异常")
    void testEvaluateEventResults_InvalidOperator() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("event1", true);
        input.put("event2", false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            processorOne.evaluateEventResults(input, "NOT");
        });

        String expectedMessage = "Unsupported condition operator: NOT";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), "应抛出包含正确错误信息的 IllegalArgumentException");
    }

    @Test
    @DisplayName("测试操作符大小写不敏感")
    void testEvaluateEventResults_OperatorCaseInsensitive() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("event1", true);
        input.put("event2", true);

        boolean resultAnd = processorOne.evaluateEventResults(input, "and");
        boolean resultOr = processorOne.evaluateEventResults(new HashMap<String, Boolean>() {{
            put("event1", false);
            put("event2", true);
        }}, "Or");

        assertTrue(resultAnd, "操作符为 'and'（小写）时，应正确处理为 AND");
        assertTrue(resultOr, "操作符为 'Or'（混合大小写）时，应正确处理为 OR");
    }

    @Test
    @DisplayName("测试 Map 中存在 null 值")
    void testEvaluateEventResults_MapContainsNull() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("event1", true);
        input.put("event2", null); // 这里假设 null 被视为 false

        // 根据原方法的逻辑，null 会在自动拆箱时抛出 NullPointerException
        // 因此，可以根据实际需求调整方法，或测试抛出异常
        Exception exception = assertThrows(NullPointerException.class, () -> {
            processorOne.evaluateEventResults(input, "AND");
        });

        assertNotNull(exception.getMessage(), "应抛出 NullPointerException");
    }
}
