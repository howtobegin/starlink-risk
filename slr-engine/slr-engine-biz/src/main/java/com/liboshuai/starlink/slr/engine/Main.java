package com.liboshuai.starlink.slr.engine;

import com.liboshuai.starlink.slr.engine.api.enums.RuleEventAttrOpEnum;
import com.liboshuai.starlink.slr.engine.api.enums.RuleEventAttrTypeEnum;

public class Main {
    public static void main(String[] args) {
        String value1 = "10.2";
        String value2 = "1.2";

        // 假设类型为 DOUBLE
        RuleEventAttrTypeEnum ruleEventAttrTypeEnum = RuleEventAttrTypeEnum.DOUBLE;
        // 假设操作符为 ">"
        RuleEventAttrOpEnum ruleEventAttrOpEnum = RuleEventAttrOpEnum.GREATER_THAN;

        boolean result = compareValues(value1, value2, ruleEventAttrTypeEnum, ruleEventAttrOpEnum);
        System.out.println("比较结果: " + result); // 输出: 比较结果: true
    }

    /**
     * 比较两个值，判断是否满足指定的关系
     *
     * @param value1                第一个值（字符串形式）
     * @param value2                第二个值（字符串形式）
     * @param ruleEventAttrTypeEnum 值的类型
     * @param ruleEventAttrOpEnum   操作符
     * @return 比较结果，满足关系返回 true，否则返回 false
     */
    public static boolean compareValues(String value1, String value2,
                                        RuleEventAttrTypeEnum ruleEventAttrTypeEnum,
                                        RuleEventAttrOpEnum ruleEventAttrOpEnum) {
        Object val1 = parseValue(value1, ruleEventAttrTypeEnum);
        Object val2 = parseValue(value2, ruleEventAttrTypeEnum);

        // 使用 Comparable 接口进行比较
        if (!(val1 instanceof Comparable) || !(val2 instanceof Comparable)) {
            throw new IllegalArgumentException("值必须实现 Comparable 接口");
        }

        @SuppressWarnings("unchecked")
        Comparable<Object> comparable1 = (Comparable<Object>) val1;
        int cmpResult = comparable1.compareTo(val2);

        switch (ruleEventAttrOpEnum) {
            case GREATER_THAN:
                return cmpResult > 0;
            case LESS_THAN:
                return cmpResult < 0;
            case GREATER_THAN_OR_EQUAL:
                return cmpResult >= 0;
            case LESS_THAN_OR_EQUAL:
                return cmpResult <= 0;
            case EQUAL:
                return cmpResult == 0;
            case NOT_EQUAL:
                return cmpResult != 0;
            default:
                throw new IllegalArgumentException("未知的操作符: " + ruleEventAttrOpEnum);
        }
    }

    /**
     * 将字符串形式的值转换为指定类型的对象
     *
     * @param value    字符串形式的值
     * @param typeEnum 值的类型枚举
     * @return 转换后的对象
     */
    private static Object parseValue(String value, RuleEventAttrTypeEnum typeEnum) {
        try {
            switch (typeEnum) {
                case BYTE:
                    return Byte.parseByte(value);
                case SHORT:
                    return Short.parseShort(value);
                case INT:
                    return Integer.parseInt(value);
                case LONG:
                    return Long.parseLong(value);
                case FLOAT:
                    return Float.parseFloat(value);
                case DOUBLE:
                    return Double.parseDouble(value);
                case BOOLEAN:
                    return Boolean.parseBoolean(value);
                case CHAR:
                    if (value.length() != 1) {
                        throw new IllegalArgumentException("字符类型的值长度必须为1");
                    }
                    return value.charAt(0);
                case STRING:
                    return value;
                default:
                    throw new IllegalArgumentException("未知的类型: " + typeEnum);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("值无法转换为类型 " + typeEnum.getType() + ": " + value, e);
        }
    }
}
