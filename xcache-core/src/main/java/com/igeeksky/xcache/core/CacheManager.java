package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.Cache;

import java.util.Collection;

/**
 * 缓存管理者接口
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public interface CacheManager {

    /**
     * 获取或创建指定名称的缓存
     *
     * @param cacheName 缓存名称，用于唯一标识一个缓存
     * @param keyType   缓存键类型
     * @param valueType 缓存值类型
     * @param <K>       泛型参数，表示键的类型
     * @param <V>       泛型参数，表示值的类型
     * @return 返回已存在或新创建的缓存对象
     * <p>
     * 此方法用于根据缓存名称和指定的键值类型获取一个缓存实例。<p>
     * 如果指定名称的缓存已经存在，则直接返回；否则，将根据指定的名称和类型创建一个新的缓存并返回。
     */
    default <K, V> Cache<K, V> getOrCreateCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return getOrCreateCache(cacheName, keyType, null, valueType, null);
    }

    /**
     * 获取或创建指定名称的缓存
     *
     * @param cacheName   缓存名称，用于唯一标识一个缓存
     * @param keyType     缓存键类型
     * @param keyParams   缓存键的泛型参数（用于较复杂的带泛型参数的键类型的序列化处理）
     * @param valueType   缓存值类型
     * @param valueParams 缓存值的泛型参数（用于较复杂的带泛型参数的值类型的序列化处理）
     * @param <K>         表示键的类型
     * @param <V>         表示值的类型
     * @return 返回已存在或新创建的缓存对象
     */
    <K, V> Cache<K, V> getOrCreateCache(String cacheName, Class<K> keyType, Class<?>[] keyParams, Class<V> valueType, Class<?>[] valueParams);

    Collection<Cache<?, ?>> getAll();

    Collection<String> getAllCacheNames();

}