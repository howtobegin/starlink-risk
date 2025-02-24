package com.liboshuai.slr.engine.api.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TemplateUtil 单元测试
 */
class TemplateUtilTest {

    /**
     * 测试基本对象属性替换
     */
    @Test
    void testReplacePlaceholders_withSimpleObject() {
        User user = new User("Alice", 25);
        String template = "Hello, ${User.name}. You are ${User.age} years old.";

        String result = TemplateUtil.replacePlaceholders(template, user);

        assertThat(result).isEqualTo("Hello, Alice. You are 25 years old.");
    }

    /**
     * 测试嵌套对象替换
     */
    @Test
    void testReplacePlaceholders_withNestedObject() {
        Address address = new Address("New York", "USA");
        User user = new User("Bob", 30, address);
        String template = "User: ${User.name}, Age: ${User.age}, City: ${User.address.city}, Country: ${User.address.country}";

        String result = TemplateUtil.replacePlaceholders(template, user);

        assertThat(result).isEqualTo("User: Bob, Age: 30, City: New York, Country: USA");
    }

    /**
     * 测试多个对象替换
     */
    @Test
    void testReplacePlaceholders_withMultipleObjects() {
        User user = new User("Dave", 40);
        Address address = new Address("Los Angeles", "USA");

        String template = "Person: ${User.name}, Age: ${User.age}, Location: ${Address.city}, ${Address.country}";

        String result = TemplateUtil.replacePlaceholders(template, user, address);

        assertThat(result).isEqualTo("Person: Dave, Age: 40, Location: Los Angeles, USA");
    }

    /**
     * 测试占位符不存在的情况
     */
    @Test
    void testReplacePlaceholders_withMissingPlaceholder() {
        User user = new User("Eve", 28);
        String template = "Hello, ${User.name}. Your job: ${User.job}.";

        String result = TemplateUtil.replacePlaceholders(template, user);

        // 未定义 job 字段，仍保持原占位符
        assertThat(result).isEqualTo("Hello, Eve. Your job: ${User.job}.");
    }

    /**
     * 处理输入为空的情况
     */
    @Test
    void testReplacePlaceholders_withNullInputs() {
        String template = "Test: ${User.name}";
        String result = TemplateUtil.replacePlaceholders(template, (Object) null);

        // 替换无效，保留占位符
        assertThat(result).isEqualTo("Test: ${User.name}");
    }

    /**
     * 处理基本数据类型
     */
    @Test
    void testReplacePlaceholders_withPrimitiveFields() {
        Stats stats = new Stats(99, true);
        String template = "Score: ${Stats.points}, Passed: ${Stats.passed}";

        String result = TemplateUtil.replacePlaceholders(template, stats);

        assertThat(result).isEqualTo("Score: 99, Passed: true");
    }

    /**
     * 用户实体类
     */
    static class User {
        private final String name;
        private final int age;
        private Address address;

        User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        User(String name, int age, Address address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }
    }

    /**
     * 地址实体类
     */
    static class Address {
        private final String city;
        private final String country;

        Address(String city, String country) {
            this.city = city;
            this.country = country;
        }
    }

    /**
     * 成绩统计
     */
    static class Stats {
        private final int points;
        private final boolean passed;

        Stats(int points, boolean passed) {
            this.points = points;
            this.passed = passed;
        }
    }
}