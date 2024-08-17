package com.igeeksky.xcache;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 从数据源读取数据
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
@FunctionalInterface
public interface CacheLoader<K, V> {

    V load(K key);

    default Map<K, V> loadAll(Set<? extends K> keys) {
        Map<K, V> map = new HashMap<>();
        for (K key : keys) {
            V value = load(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

}