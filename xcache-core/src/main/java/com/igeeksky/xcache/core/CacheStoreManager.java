package com.igeeksky.xcache.core;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.CacheManager;
import com.igeeksky.xcache.CacheProvider;
import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.common.CacheLevel;
import com.igeeksky.xcache.beans.BeanHolder;
import com.igeeksky.xcache.config.CacheProperties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public class CacheStoreManager implements CacheManager {

    private final CacheLevel cacheLevel;
    private final Map<String, CacheProvider> cacheProviders = new HashMap<>(8);
    private final ConcurrentMap<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();

    public CacheStoreManager(CacheProperties cacheProperties) {
        this.cacheLevel = cacheProperties.getCacheLevel();
        List<String> stores = cacheProperties.getStores();
        BeanContext beanContext = cacheProperties.getBeanContext();
        for (String id : stores) {
            BeanHolder beanHolder = beanContext.getBeanHolder(id);
            CacheProvider cacheProvider = beanHolder.getBean(CacheProvider.class);
            cacheProviders.put(id, cacheProvider);
        }
    }

    @Override
    public CacheLevel getCacheLevel() {
        return cacheLevel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Cache<K, V> get(String name, CacheProperties cacheProperties, Class<K> keyType, Class<V> valueType) {
        CacheProvider cacheProvider = cacheProviders.get(cacheProperties.getDefaultStore());
        return (Cache<K, V>) cacheMap.computeIfAbsent(name, nameKey -> cacheProvider.get(name, cacheProperties, keyType, valueType));
    }

    @Override
    public Collection<Cache<?, ?>> getAll() {
        return Collections.unmodifiableCollection(cacheMap.values());
    }

    @Override
    public Collection<String> getAllCacheNames() {
        return Collections.unmodifiableCollection(cacheMap.keySet());
    }
}
