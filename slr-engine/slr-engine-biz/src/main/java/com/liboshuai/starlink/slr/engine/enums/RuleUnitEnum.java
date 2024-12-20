package com.liboshuai.starlink.slr.engine.enums;

/**
 * @Author liboshuai
 * @Date 2023/11/6 17:08
 */
public enum RuleUnitEnum {

    MILLISECOND("MILLISECOND", "毫秒"),
    SECOND("SECOND", "秒"),
    MINUTE("MINUTE", "分钟"),
    HOUR("HOUR", "小时"),
    DAY("DAY", "天"),
    WEEK("WEEK", "周"),
    MONTH("MONTH", "月"),
    YEAR("YEAR", "年");

    private final String enUnit;
    private final String cnUnit;

    public String getEnUnit() {
        return this.enUnit;
    }

    public String getCnUnit() {
        return this.cnUnit;
    }

    RuleUnitEnum(String enUnit, String cnUnit) {
        this.enUnit = enUnit;
        this.cnUnit = cnUnit;
    }

    /**
     * 将英文单位转为中文单位
     * @param enUnit 英文单位
     * @return 中文单位
     */
    public static String convertEnUnitToCnUnit(String enUnit) {
        for (RuleUnitEnum ruleUnitEnum : RuleUnitEnum.values()) {
            if (ruleUnitEnum.getEnUnit().equals(enUnit)) {
                return ruleUnitEnum.getCnUnit();
            }
        }
        return "未知单位";
    }

    /**
     * 将中文单位转为英文单位
     * @param cnUnit 中文单位
     * @return 英文单位
     */
    public static String convertCnUnitToEnUnit(String cnUnit) {
        for (RuleUnitEnum ruleUnitEnum : RuleUnitEnum.values()) {
            if (ruleUnitEnum.getCnUnit().equals(cnUnit)) {
                return ruleUnitEnum.getEnUnit();
            }
        }
        return "未知单位";
    }

    /**
     * 根据中文单位获取枚举
     * @param cnUnit 中文单位
     * @return 枚举
     */
    public static RuleUnitEnum fromCnUnit(String cnUnit) {
        for (RuleUnitEnum ruleUnitEnum : RuleUnitEnum.values()) {
            if (ruleUnitEnum.getCnUnit().equals(cnUnit)) {
                return ruleUnitEnum;
            }
        }
        return null;
    }

    /**
     * 根据英文单位获取枚举
     * @param enUnit 英文单位
     * @return 枚举
     */
    public static RuleUnitEnum fromEnUnit(String enUnit) {
        for (RuleUnitEnum ruleUnitEnum : RuleUnitEnum.values()) {
            if (ruleUnitEnum.getEnUnit().equals(enUnit)) {
                return ruleUnitEnum;
            }
        }
        return null;
    }
}
