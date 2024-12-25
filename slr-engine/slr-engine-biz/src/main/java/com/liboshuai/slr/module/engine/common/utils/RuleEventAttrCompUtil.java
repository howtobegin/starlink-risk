package com.liboshuai.slr.module.engine.common.utils;

import com.liboshuai.slr.module.engine.enums.RuleEventAttrOpEnum;
import com.liboshuai.slr.module.engine.enums.RuleEventAttrTypeEnum;

import java.util.Objects;

public class RuleEventAttrCompUtil {

    /**
     * 比较两个值，判断是否满足指定的关系
     *
     * @param kafkaEventAttrValue kafka事件属性值（字符串形式）
     * @param ruleEventAttrValue  规则事件属性值（字符串形式）
     * @param ruleEventAttrType   规则事件属性类型
     * @param ruleEventAttrOp     规则事件属性操作符
     * @return 比较结果，满足关系返回 true，否则返回 false
     */
    public static boolean compareValues(String kafkaEventAttrValue, String ruleEventAttrValue,
                                        String ruleEventAttrType, String ruleEventAttrOp) {
        if (Objects.isNull(kafkaEventAttrValue)) {
            return false;
        }
        if (Objects.isNull(ruleEventAttrValue)) {
            throw new IllegalArgumentException("无效的 ruleEventAttrValue：不能为空！");
        }
        if (Objects.isNull(ruleEventAttrType)) {
            throw new IllegalArgumentException("无效的 ruleEventAttrValue：不能为空！");
        }
        if (Objects.isNull(ruleEventAttrOp)) {
            throw new IllegalArgumentException("无效的 ruleEventAttrOp：不能为空！");
        }
        RuleEventAttrTypeEnum ruleEventAttrTypeEnum = RuleEventAttrTypeEnum.fromCode(ruleEventAttrType);
        RuleEventAttrOpEnum ruleEventAttrOpEnum = RuleEventAttrOpEnum.fromSymbol(ruleEventAttrOp);
        Object val1 = parseValue(kafkaEventAttrValue, ruleEventAttrTypeEnum);
        Object val2 = parseValue(ruleEventAttrValue, ruleEventAttrTypeEnum);

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
