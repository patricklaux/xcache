package com.igeeksky.xcache.spring;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.core.CacheManager;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Spring CacheManager 实现类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-20
 */
public class SpringCacheManager implements org.springframework.cache.CacheManager {

    private final CacheManager cacheManager;
    private final ConcurrentMap<String, SpringCache> caches = new ConcurrentHashMap<>();

    public SpringCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public org.springframework.cache.Cache getCache(@NonNull String name) {
        return this.getCache(name, Object.class, Object.class);
    }

    @SuppressWarnings("unchecked")
    public <K, V> org.springframework.cache.Cache getCache(String name, Class<K> keyType, Class<V> valueType) {
        SpringCache springCache = caches.get(name);
        return (null != springCache) ? springCache :
                caches.computeIfAbsent(name, nameKey -> {
                    Cache<K, V> cache = cacheManager.getOrCreateCache(nameKey, keyType, valueType);
                    return new SpringCache((Cache<Object, Object>) cache);
                });
    }

    @Override
    public @NonNull
    Collection<String> getCacheNames() {
        return Collections.unmodifiableCollection(caches.keySet());
    }

}
