package com.igeeksky.xcache.common;


import com.igeeksky.xtool.core.collection.Maps;

import java.util.Map;
import java.util.Set;

/**
 * 回源取值，用于从数据源读取数据
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
@FunctionalInterface
public interface CacheLoader<K, V> {

    /**
     * 单个回源取值
     *
     * @param key 要回源取值的键
     * @return 如果有值，返回值；如果无值，返回 null
     */
    V load(K key);

    /**
     * 批量回源取值
     *
     * @param keys 要回源取值的键集
     * @return 返回键值对集合，不能返回 null
     */
    default Map<K, V> loadAll(Set<? extends K> keys) {
        Map<K, V> map = Maps.newHashMap(keys.size());
        for (K key : keys) {
            V value = load(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

}