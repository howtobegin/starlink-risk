package com.liboshuai.starlink;

import com.liboshuai.starlink.slr.engine.api.enums.RuleCondCombOpEnum;
import com.liboshuai.starlink.slr.engine.processor.impl.ProcessorOne;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class EvaluateEventResultsTest {

    private ProcessorOne processorOne;

    @BeforeEach
    public void setUp() {
        processorOne = new ProcessorOne();
    }

    @Test
    public void testNullMap() {
        boolean result = processorOne.evaluateEventResults(null, RuleCondCombOpEnum.AND.getCode());
        Assertions.assertFalse(result, "当传入的Map为null时，应返回false");
    }

    @Test
    public void testEmptyMap() {
        Map<String, Boolean> emptyMap = new HashMap<>();
        boolean result = processorOne.evaluateEventResults(emptyMap, RuleCondCombOpEnum.AND.getCode());
        Assertions.assertFalse(result, "当传入的Map为空时，应返回false");
    }

    @Test
    public void testAndOperatorAllTrue() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("event1", true);
        map.put("event2", true);
        map.put("event3", true);

        boolean result = processorOne.evaluateEventResults(map, RuleCondCombOpEnum.AND.getCode());
        Assertions.assertTrue(result, "所有事件结果为true时，AND操作应返回true");
    }

    @Test
    public void testAndOperatorWithFalse() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("event1", true);
        map.put("event2", false);
        map.put("event3", true);

        boolean result = processorOne.evaluateEventResults(map, RuleCondCombOpEnum.AND.getCode());
        Assertions.assertFalse(result, "存在一个false时，AND操作应返回false");
    }

    @Test
    public void testOrOperatorAllFalse() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("event1", false);
        map.put("event2", false);
        map.put("event3", false);

        boolean result = processorOne.evaluateEventResults(map, RuleCondCombOpEnum.OR.getCode());
        Assertions.assertFalse(result, "所有事件结果为false时，OR操作应返回false");
    }

    @Test
    public void testOrOperatorWithTrue() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("event1", false);
        map.put("event2", true);
        map.put("event3", false);

        boolean result = processorOne.evaluateEventResults(map, RuleCondCombOpEnum.OR.getCode());
        Assertions.assertTrue(result, "存在一个true时，OR操作应返回true");
    }

    @Test
    public void testAndOperatorSingleTrue() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("event1", true);

        boolean result = processorOne.evaluateEventResults(map, RuleCondCombOpEnum.AND.getCode());
        Assertions.assertTrue(result, "单个true的情况下，AND操作应返回true");
    }

    @Test
    public void testAndOperatorSingleFalse() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("event1", false);

        boolean result = processorOne.evaluateEventResults(map, RuleCondCombOpEnum.AND.getCode());
        Assertions.assertFalse(result, "单个false的情况下，AND操作应返回false");
    }

    @Test
    public void testOrOperatorSingleTrue() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("event1", true);

        boolean result = processorOne.evaluateEventResults(map, RuleCondCombOpEnum.OR.getCode());
        Assertions.assertTrue(result, "单个true的情况下，OR操作应返回true");
    }

    @Test
    public void testOrOperatorSingleFalse() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("event1", false);

        boolean result = processorOne.evaluateEventResults(map, RuleCondCombOpEnum.OR.getCode());
        Assertions.assertFalse(result, "单个false的情况下，OR操作应返回false");
    }

    @Test
    public void testInvalidOperator() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("event1", true);
        map.put("event2", false);

        // 假设对于无效的操作符，方法应返回false
        int invalidOperator = 3;
        boolean result = processorOne.evaluateEventResults(map, invalidOperator);
        Assertions.assertFalse(result, "无效的操作符应返回false");
    }
}