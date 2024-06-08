package com.igeeksky.xcache;


import com.igeeksky.xcache.common.CacheValue;

import java.util.Map;
import java.util.Set;

/**
 * 缓存接口
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public interface Base<K, V> {

    /**
     * 根据键从缓存中读取值
     *
     * @param key 键
     * @return CacheValue – 值的包装类
     * <p>1. CacheValue 为空，表示 key 不存在于缓存中。</p>
     * <p>2. CacheValue 不为空，表示 key 存在于缓存中：</p>
     * <p>2.1. CacheValue 内部的 value 不为空，缓存的是正常值；</p>
     * <p>2.2. CacheValue 内部的 value 为空，缓存的是空值；</p>
     */
    CacheValue<V> get(K key);

    /**
     * 根据键集从缓存中读取值
     *
     * @param keys 多个键的集合
     * @return KeyValue – 键值对的包装类
     * <p>1. CacheValue 为空，表示 key 不存在于缓存中。</p>
     * <p>2. CacheValue 不为空，表示 key 存在于缓存中：</p>
     * <p>2.1. CacheValue 内部的 value 不为空，缓存的是正常值；</p>
     * <p>2.2. CacheValue 内部的 value 为空，缓存的是空值；</p>
     */
    Map<K, CacheValue<V>> getAll(Set<? extends K> keys);

    /**
     * 将单个键值对存入缓存
     *
     * @param key   键
     * @param value 值
     */
    void put(K key, V value);

    /**
     * 将多个键值对存入缓存
     *
     * @param keyValues 键值对的集合
     */
    void putAll(Map<? extends K, ? extends V> keyValues);

    /**
     * 根据键将数据逐出缓存
     *
     * @param key 键
     */
    void evict(K key);

    /**
     * 根据键集将数据逐出缓存
     *
     * @param keys 键集
     */
    void evictAll(Set<? extends K> keys);

    /**
     * 清空缓存中的所有数据
     */
    void clear();

}