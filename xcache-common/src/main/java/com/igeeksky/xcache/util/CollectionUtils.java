package com.igeeksky.xcache.util;

import java.util.*;


/**
 * 集合容器工具类
 *
 * @author Patrick.Lau
 * @since 0.0.1
 */
public abstract class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return (collection != null && !collection.isEmpty());
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return (map != null && !map.isEmpty());
    }

    /**
     * 将源对象的键值对合并到目标对象 <br/>
     * if(!target.containsKey(key))   target.put(key, source.get(key))
     *
     * @param source 源对象
     * @param target 目标对象
     * @return target
     */
    public static <K, V> Map<K, V> merge(Map<K, V> source, Map<K, V> target) {
        Objects.requireNonNull(source, "source map must not be null");
        Objects.requireNonNull(target, "target map must not be null");
        source.forEach((key, value) -> target.computeIfAbsent(key, k -> value));
        return target;
    }

    /**
     * 将源对象的键值对复制到目标对象 <br/>
     *
     * @param source 源对象
     * @param target 目标对象
     * @return target
     */
    public static <K, V> Map<K, V> clone(Map<K, V> source, Map<K, V> target) {
        Objects.requireNonNull(source, "source map must not be null");
        Objects.requireNonNull(target, "target map must not be null");
        source.forEach((k, v) -> target.put(k, v));
        return target;
    }

    public static <T> List<T> reverse(List<T> list) {
        int size = list.size();
        List<T> reverse = new ArrayList<>();
        for (int i = size - 1; i >= 0; i--) {
            reverse.add(list.get(i));
        }
        return reverse;
    }

}
