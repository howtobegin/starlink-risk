package com.liboshuai.slr.framework.common.util.object.reflect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射工具类
 */
@Slf4j
public class ReflectUtils {

    // 缓存Lambda表达式和对应字段的映射，避免重复计算
    private static final Map<SFunction<?>, Field> FUNCTION_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取字段名称。
     *
     * @param function Lambda表达式
     * @param <T>      类型参数
     * @return 字段名称
     */
    public static <T> String getFieldName(SFunction<T> function) {
        Field field = ReflectUtils.getField(function);
        return field.getName();
    }

    /**
     * 获取字段值。
     *
     * @param obj     对象实例
     * @param function Lambda表达式
     * @param <T>      类型参数
     * @return 字段值
     */
    public static <T> Object getFieldValue(Object obj, SFunction<T> function) {
        try {
            Field field = getField(function);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access field value.", e);
        }
    }

    /**
     * 设置字段值。
     *
     * @param obj     对象实例
     * @param function Lambda表达式
     * @param value   要设置的值
     * @param <T>      类型参数
     */
    public static <T> void setFieldValue(Object obj, SFunction<T> function, Object value) {
        try {
            Field field = getField(function);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to set field value.", e);
        }
    }

    /**
     * 检查字段是否存在。
     *
     * @param function Lambda表达式
     * @param <T>      类型参数
     * @return 如果字段存在，返回true，否则返回false
     */
    public static <T> boolean fieldExists(SFunction<T> function) {
        try {
            getField(function);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * 获取所有字段。
     *
     * @param clazz 类
     * @return 字段列表
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 获取字段对象。
     *
     * @param function Lambda表达式
     * @param <T>      类型参数
     * @return 字段对象
     */
    public static <T> Field getField(SFunction<T> function) {
        return FUNCTION_CACHE.computeIfAbsent(function, ReflectUtils::findField);
    }

    /**
     * 查找字段对象。
     *
     * @param function Lambda表达式
     * @param <T>      类型参数
     * @return 字段对象
     */
    public static <T> Field findField(SFunction<T> function) {
        // 第1步 获取SerializedLambda
        final SerializedLambda serializedLambda = getSerializedLambda(function);
        // 第2步 implMethodName 即为Field对应的Getter方法名
        final String implClass = serializedLambda.getImplClass();
        final String implMethodName = serializedLambda.getImplMethodName();
        final String fieldName = convertToFieldName(implMethodName);
        // 第3步 通过Spring的反射工具类获取Class中定义的Field
        final Field field = getField(fieldName, serializedLambda);

        // 第4步 如果没有找到对应的字段应该抛出异常
        if (field == null) {
            throw new RuntimeException("No such class 「" + implClass + "」 field 「" + fieldName + "」.");
        }
        // 设置字段可访问
        field.setAccessible(true);
        return field;
    }

    /**
     * 获取字段对象。
     *
     * @param fieldName        字段名称
     * @param serializedLambda 序列化的Lambda表达式
     * @return 字段对象
     */
    static Field getField(String fieldName, SerializedLambda serializedLambda) {
        try {
            // 获取的Class是字符串，并且包名是“/”分割，需要替换成“.”，才能获取到对应的Class对象
            String declaredClass = serializedLambda.getImplClass().replace("/", ".");
            Class<?> aClass = Class.forName(declaredClass, false, ClassUtils.getDefaultClassLoader());
            return ReflectionUtils.findField(aClass, fieldName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("get class field exception.", e);
        }
    }

    /**
     * 将getter方法名转换为字段名。
     *
     * @param getterMethodName Getter方法名
     * @return 字段名称
     */
    static String convertToFieldName(String getterMethodName) {
        // 获取方法名
        String prefix = null;
        if (getterMethodName.startsWith("get")) {
            prefix = "get";
        } else if (getterMethodName.startsWith("is")) {
            prefix = "is";
        } else {
            throw new IllegalArgumentException("Invalid getter method: " + getterMethodName);
        }

        String withoutPrefix = getterMethodName.substring(prefix.length());

        if (withoutPrefix.isEmpty()) {
            throw new IllegalArgumentException("Getter method name is too short: " + getterMethodName);
        }

        // 转换首字母为小写
        return Introspector.decapitalize(withoutPrefix);
    }

    /**
     * 获取SerializedLambda对象。
     *
     * @param function Lambda表达式
     * @param <T>      类型参数
     * @return SerializedLambda对象
     */
    static <T> SerializedLambda getSerializedLambda(SFunction<T> function) {
        try {
            Method method = function.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            return (SerializedLambda) method.invoke(function);
        } catch (Exception e) {
            throw new RuntimeException("get SerializedLambda exception.", e);
        }
    }
}