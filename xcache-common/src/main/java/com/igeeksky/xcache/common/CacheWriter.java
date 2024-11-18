package com.igeeksky.xcache.common;

import java.util.Map;
import java.util.Set;

/**
 * 数据回写：缓存数据写入数据源
 * <p>
 * 如果实现类同步写入数据源，则为 write-through 模式；
 * 如果实现类异步写入数据源，则为 write-behind 模式。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/2
 */
public interface CacheWriter<K, V> {

    /**
     * 删除数据源的数据
     *
     * @param key 缓存键
     */
    void delete(K key);

    /**
     * 批量删除数据源的数据
     *
     * @param keys 缓存键集合
     */
    void deleteAll(Set<? extends K> keys);

    /**
     * 数据存入数据源
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void write(K key, V value);

    /**
     * 批量存入数据源
     *
     * @param keyValues 缓存键值对集合
     */
    void writeAll(Map<? extends K, ? extends V> keyValues);

}
