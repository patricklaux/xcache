package com.igeeksky.xcache;

import com.igeeksky.xcache.common.CacheLevel;
import com.igeeksky.xcache.config.CacheProperties;

import java.util.Collection;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public interface CacheManager {

    CacheLevel getCacheLevel();

    <K, V> Cache<K, V> get(String name, CacheProperties cacheProperties, Class<K> keyClazz, Class<V> valueClazz);

    Collection<Cache<?, ?>> getAll();

    Collection<String> getAllCacheNames();

}
