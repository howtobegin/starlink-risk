package com.liboshuai.slr.engine.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串消息模板替换 工具类
 */
@Slf4j
public class TemplateUtil {


    /**
     * 占位符正则表达式，匹配${ClassName.fieldName}或${ClassName.fieldName.nestedField}形式
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^{}]+)\\}");

    /**
     * 替换模板中的占位符
     *
     * @param template 包含占位符的模板字符串
     * @param objects 用于提取值的对象列表
     * @return 替换后的字符串
     */
    public static String replacePlaceholders(String template, Object... objects) {
        if (StringUtils.isBlank(template) || objects == null || objects.length == 0) {
            return template;
        }

        // 创建对象名称到对象的映射
        Map<String, Object> objectMap = new HashMap<>();
        for (Object obj : objects) {
            if (obj != null) {
                String className = obj.getClass().getSimpleName();
                objectMap.put(className, obj);
            }
        }

        // 用于存储最终的替换值
        Map<String, String> valueMap = new HashMap<>();

        // 查找所有占位符
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String[] parts = placeholder.split("\\.");

            if (parts.length < 2) {
                continue;
            }

            String className = parts[0];
            Object obj = objectMap.get(className);

            if (obj == null) {
                continue;
            }

            try {
                // 提取对象的值
                Object value = extractValue(obj, parts, 1);
                if (value != null) {
                    valueMap.put(placeholder, value.toString());
                }
            } catch (Exception e) {
                log.warn("Error extracting value for placeholder: {}", placeholder, e);
            }
        }

        // 使用 StringSubstitutor 替换占位符
        StringSubstitutor substitutor = new StringSubstitutor(valueMap, "${", "}");
        return substitutor.replace(template);
    }

    /**
     * 递归提取对象的值
     *
     * @param obj 当前对象
     * @param parts 占位符拆分的各部分
     * @param index 当前处理的部分索引
     * @return 提取的值
     */
    @SuppressWarnings("unchecked")
    private static Object extractValue(Object obj, String[] parts, int index) throws Exception {
        if (obj == null || index >= parts.length) {
            return obj;
        }

        String fieldName = parts[index];

        // 处理集合索引访问 例如: ruleCondGroup.0.windowValue
        if (obj instanceof List && fieldName.matches("\\d+")) {
            int listIndex = Integer.parseInt(fieldName);
            List<?> list = (List<?>) obj;
            if (listIndex >= 0 && listIndex < list.size()) {
                return extractValue(list.get(listIndex), parts, index + 1);
            }
            return null;
        }

        // 处理Map访问 例如: eventAttrMap.bankName
        if (obj instanceof Map) {
            Map<String, ?> map = (Map<String, ?>) obj;
            Object value = map.get(fieldName);
            return index + 1 < parts.length ? extractValue(value, parts, index + 1) : value;
        }

        // 通过反射访问字段
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(obj);
                return index + 1 < parts.length ? extractValue(value, parts, index + 1) : value;
            }
        } catch (NoSuchFieldException e) {
            log.debug("Field {} not found in {}", fieldName, obj.getClass().getName());
        }

        return null;
    }

    /**
     * 在类及其父类中查找字段
     *
     * @param clazz 类对象
     * @param fieldName 字段名
     * @return 字段对象
     * @throws NoSuchFieldException 如果字段不存在
     */
    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return findField(superClass, fieldName);
            }
            throw e;
        }
    }
}
