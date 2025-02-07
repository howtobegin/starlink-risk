package com.liboshuai.slr.engine.api.enums;

import com.liboshuai.slr.framework.common.core.StringArrayValuable;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 规则事件属性操作符
 */
@Getter
public enum RuleEventAttrOpEnum implements StringArrayValuable {
    GREATER_THAN(">", "大于"),
    LESS_THAN("<", "小于"),
    GREATER_THAN_OR_EQUAL(">=", "大于等于"),
    LESS_THAN_OR_EQUAL("<=", "小于等于"),
    EQUAL("==", "等于"),
    NOT_EQUAL("!=", "不等于");

    public static final String[] ARRAYS = Arrays.stream(values()).map(RuleEventAttrOpEnum::getSymbol).toArray(String[]::new);

    /**
     * 符号
     */
    private final String symbol;
    /**
     * 描述
     */
    private final String desc;

    RuleEventAttrOpEnum(String symbol, String desc) {
        this.symbol = symbol;
        this.desc = desc;
    }

    /**
     * 通过符号查找枚举值
     *
     * @param symbol 符号
     * @return 对应的 RuleEventAttributeOpEnum
     * @throws IllegalArgumentException 如果没有匹配的符号
     */
    public static RuleEventAttrOpEnum fromSymbol(String symbol) {
        for (RuleEventAttrOpEnum operator : RuleEventAttrOpEnum.values()) {
            if (Objects.equals(operator.getSymbol(), symbol)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("未知的符号: " + symbol);
    }

    /**
     * 通过描述查找枚举值
     *
     * @param desc 描述
     * @return 对应的 RuleEventAttributeOpEnum
     * @throws IllegalArgumentException 如果没有匹配的描述
     */
    public static RuleEventAttrOpEnum fromDesc(String desc) {
        for (RuleEventAttrOpEnum operator : RuleEventAttrOpEnum.values()) {
            if (operator.getDesc().equals(desc)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("未知的描述: " + desc);
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
