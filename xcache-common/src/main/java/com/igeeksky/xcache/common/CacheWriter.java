package com.igeeksky.xcache.common;

import java.util.Map;
import java.util.Set;

/**
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/2
 */
public interface CacheWriter<K, V> {

    void delete(K key);

    void deleteAll(Set<? extends K> keys);

    void write(K key, V value);

    void writeAll(Map<? extends K, ? extends V> keyValues);

}
