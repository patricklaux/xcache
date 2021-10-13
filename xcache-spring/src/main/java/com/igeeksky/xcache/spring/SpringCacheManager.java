package com.igeeksky.xcache.spring;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.common.annotation.NotNull;
import com.igeeksky.xcache.core.XcacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-20
 */
public class SpringCacheManager implements CacheManager {

    private final XcacheManager xcacheManager;
    private final ConcurrentMap<String, SpringCache> cacheMap = new ConcurrentHashMap<>();

    public SpringCacheManager(XcacheManager xcacheManager) {
        this.xcacheManager = xcacheManager;
    }

    @Override
    public org.springframework.cache.Cache getCache(@NonNull String name) {
        return this.getCache(name, Object.class, Object.class);
    }

    @SuppressWarnings("unchecked")
    public <K, V> org.springframework.cache.Cache getCache(String name, Class<K> keyType, Class<V> valueType) {
        SpringCache cache = cacheMap.get(name);
        if (null != cache) {
            return cache;
        }
        return cacheMap.computeIfAbsent(name, nameKey -> {
            Cache<K, V> kvCache = xcacheManager.get(nameKey, keyType, valueType);
            return new SpringCache((Cache<Object, Object>) kvCache);
        });
    }

    @Override
    public @NonNull Collection<String> getCacheNames() {
        return Collections.unmodifiableCollection(cacheMap.keySet());
    }

}
