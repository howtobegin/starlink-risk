package com.liboshuai.slr.framework.common.util.json;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类
 *
 * @author 李博帅
 */
@Slf4j
public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 忽略 null 值
        objectMapper.registerModules(new JavaTimeModule()); // 解决 LocalDateTime 的序列化
    }

    /**
     * 初始化 objectMapper 属性
     * <p>
     * 通过这样的方式，使用 Spring 创建的 ObjectMapper Bean
     *
     * @param objectMapper ObjectMapper 对象
     */
    public static void init(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
    }

    @SneakyThrows
    public static String toJsonString(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows
    public static byte[] toJsonByte(Object object) {
        return objectMapper.writeValueAsBytes(object);
    }

    @SneakyThrows
    public static String toJsonPrettyString(Object object) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObjectWithType(String text, TypeReference<T> typeReference) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (IOException e) {
            log.error("json parse err, json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, String path, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            JsonNode treeNode = objectMapper.readTree(text);
            JsonNode pathNode = treeNode.path(path);
            return objectMapper.readValue(pathNode.toString(), clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Type type) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructType(type));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字符串解析成指定类型的对象
     * 使用 {@link #parseObject(String, Class)} 时，在@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) 的场景下，
     * 如果 text 没有 class 属性，则会报错。此时，使用这个方法，可以解决。
     *
     * @param text 字符串
     * @param clazz 类型
     * @return 对象
     */
    public static <T> T parseObject2(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        return JSONUtil.toBean(text, clazz);
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", bytes, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析 JSON 字符串成指定类型的对象，如果解析失败，则返回 null
     *
     * @param text 字符串
     * @param typeReference 类型引用
     * @return 指定类型的对象
     */
    public static <T> T parseObjectQuietly(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String text, String path, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            JsonNode treeNode = objectMapper.readTree(text);
            JsonNode pathNode = treeNode.path(path);
            return objectMapper.readValue(pathNode.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseTree(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    public static JsonNode parseTree(byte[] text) {
        try {
            return objectMapper.readTree(text);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将下划线命名的 JSON 字符串解析成指定类型的对象（驼峰命名）
     *
     * @param text  JSON 字符串
     * @param clazz 目标类型
     * @return 解析后的对象
     */
    public static <T> T parseObjectFromUnderscore(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            // 设置命名策略为下划线转驼峰
            objectMapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
            return objectMapper.readValue(text, clazz);
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        } finally {
            // 恢复默认命名策略
            objectMapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE);
        }
    }

    /**
     * 将 Map 数据（键为下划线命名）转换为 Java 实体类（驼峰命名）
     *
     * @param structMap 要转换的 Map
     * @param clazz     Java 实体类的 Class
     * @param <T>       Java 实体类类型
     * @return 转换后的实体类对象
     */
    public static <T> T toEntityFromUnderscoreMap(Map<String, String> structMap, Class<T> clazz) {
        if (structMap == null || structMap.isEmpty()) {
            return null;
        }
        try {
            // 创建临时 ObjectMapper，并设置命名策略为下划线转驼峰
            ObjectMapper mapper = objectMapper.copy();
            mapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
            // 将 Map 转换为 JSON 字符串
            String jsonString = mapper.writeValueAsString(structMap);
            // 将 JSON 字符串反序列化为目标实体类
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.error("Failed to convert structMap to entity, structMap: {}", structMap, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象序列化为字段名为大写下划线形式的 JSON 字符串
     *
     * @param object 要序列化的对象
     * @return JSON 字符串
     */
    @SneakyThrows
    public static String toJsonStringWithUpperSnakeCaseKeys(Object object) {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = objectMapper.copy();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_SNAKE_CASE);
        return mapper.writeValueAsString(object);
    }

    public static boolean isJson(String text) {
        return JSONUtil.isTypeJSON(text);
    }

}
