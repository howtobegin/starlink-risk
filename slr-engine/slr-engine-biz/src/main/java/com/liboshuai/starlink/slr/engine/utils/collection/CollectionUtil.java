package com.liboshuai.starlink.slr.engine.utils.collection;

import java.util.Collection;
import java.util.Map;

/**
 * 集合工具类，用于简化常见的集合操作。
 */
public final class CollectionUtil {

    // 私有构造函数，防止实例化
    private CollectionUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 判断集合是否为空或包含任何 null 元素。
     *
     * @param collection 需要检查的集合
     * @return 如果集合为空或包含任意 null 元素，则返回 true；否则返回 false
     */
    public static boolean isEmptyOrContainsNulls(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return true;
        }
        for (Object item : collection) {
            if (item == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断 Collection 是否为空。
     *
     * @param collection 需要检查的 Collection
     * @return 如果 Collection 为空，则返回 true；否则返回 false
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断 Map 是否为空。
     *
     * @param map 需要检查的 Map
     * @return 如果 Map 为空，则返回 true；否则返回 false
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}
