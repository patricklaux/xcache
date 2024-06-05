package com.igeeksky.xcache;

import java.util.Collection;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public interface CacheManager {

    /**
     * @param cacheName   缓存名称
     * @param keyType     键class
     * @param valueType   值class
     * @param valueParams 值泛型
     * @param <K>         键类型
     * @param <V>         值类型
     * @return 缓存
     */
    <K, V> Cache<K, V> getOrCreateCache(String cacheName, Class<K> keyType, Class<V> valueType, Class<?>[] valueParams);

    Collection<Cache<?, ?>> getAll();

    Collection<String> getAllCacheNames();

}
