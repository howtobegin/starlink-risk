package com.liboshuai.slr.engine.api.utils;

import org.apache.commons.text.StringSubstitutor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字符串消息模板替换 工具类
 */
public class TemplateUtil {

    private static final String SEPARATOR = ".";

    /**
     * 替换模板中的占位符
     *
     * @param template 消息模板，支持占位符如 ${KafkaPojo.channel}
     * @param objects  需要绑定的对象，可以是多个对象
     * @return 替换占位符后的字符串
     */
    public static String replacePlaceholders(String template, Object... objects) {
        Map<String, String> placeholderMap = new HashMap<>();

        for (Object obj : objects) {
            if (obj != null) {
                String objectPrefix = obj.getClass().getSimpleName(); // 类名作为前缀
                populatePlaceholderMap(objectPrefix, obj, placeholderMap);
            }
        }

        // 使用 StringSubstitutor 进行占位符替换
        StringSubstitutor substitutor = new StringSubstitutor(placeholderMap);
        return substitutor.replace(template);
    }

    /**
     * 展开对象的所有字段，构造占位符对应的值
     *
     * @param parentPrefix   当前字段的路径前缀
     * @param obj            当前对象
     * @param placeholderMap 用于存储占位符和实际值的映射表
     */
    private static void populatePlaceholderMap(String parentPrefix, Object obj, Map<String, String> placeholderMap) {
        if (obj == null) {
            return;
        }

        // 如果是 Map 类型，递归处理其键值对
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String mapKey = parentPrefix + SEPARATOR + entry.getKey();
                populatePlaceholderMap(mapKey, entry.getValue(), placeholderMap);
            }
        }
        // 如果是 List 类型，递归处理每个元素
        else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                String listKey = parentPrefix + SEPARATOR + i;
                populatePlaceholderMap(listKey, list.get(i), placeholderMap);
            }
        }
        // 如果是基本类型、字符串或其他简单值，直接放入占位符映射
        else if (isPrimitiveOrWrapper(obj.getClass()) || obj instanceof String) {
            placeholderMap.put(parentPrefix, obj.toString());
        }
        // 如果是复杂对象，递归展开其字段
        else {
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true); // 允许访问私有字段
                try {
                    Object value = field.get(obj); // 获取字段值
                    String fieldKey = parentPrefix + SEPARATOR + field.getName();
                    populatePlaceholderMap(fieldKey, value, placeholderMap);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error accessing field: " + field.getName(), e);
                }
            }
        }
    }

    /**
     * 判断一个类型是否为基本类型或其包装类型
     *
     * @param clazz 类型
     * @return 是否为基本类型或包装类型
     */
    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == Boolean.class || clazz == Byte.class ||
                clazz == Character.class || clazz == Short.class || clazz == Integer.class ||
                clazz == Long.class || clazz == Float.class || clazz == Double.class;
    }
}
