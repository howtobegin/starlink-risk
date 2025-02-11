package com.liboshuai.slr.framework.redis.core.serializer;

import com.liboshuai.slr.framework.common.constants.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

/**
 * 统一前缀序列化
 */
@Slf4j
public class PrefixRedisSerializer extends StringRedisSerializer {

    private final String namespace;

    public PrefixRedisSerializer(String namespace) {
        this.namespace = namespace;
    }

    /**
     * 序列化
     *
     * @param key 键
     * @return 序列化后的字节数组
     */
    @Override
    public byte[] serialize(String key) {
        if (key == null) {
            return new byte[0];
        }
        // 拼接前缀
        String keyPrefix = namespace + RedisKeyConstants.REDIS_KEY_SPLIT;
        String realKey = keyPrefix + key;
        return super.serialize(realKey);
    }

    /**
     * 反序列化
     *
     * @param bytes 序列化后的字节数组
     * @return 反序列化后的键
     */
    @Override
    public String deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String key = new String(bytes, StandardCharsets.UTF_8);
        // 拼接前缀
        String keyPrefix = namespace + RedisKeyConstants.REDIS_KEY_SPLIT;
        if (key.startsWith(keyPrefix)) {
            return key.substring(keyPrefix.length());
        }
        log.warn("Key不以预期前缀开始: {}", key);
        return key;
    }
}