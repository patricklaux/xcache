package com.igeeksky.xcache.caffeine;


import com.github.benmanes.caffeine.cache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.store.AbstractEmbedStore;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.Map;
import java.util.Set;

/**
 * Caffeine 作为存储层
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
public class CaffeineStore<V> extends AbstractEmbedStore<V> {

    private final Cache<String, CacheValue<Object>> store;

    public CaffeineStore(Cache<String, CacheValue<Object>> store, CaffeineConfig<V> config) {
        super(config.isEnableNullValue(), config.isEnableCompressValue(),
                config.isEnableSerializeValue(), config.getValueCompressor(), config.getValueCodec());
        this.store = store;
    }

    @Override
    public CacheValue<V> get(String key) {
        return fromStoreValue(store.getIfPresent(key));
    }

    @Override
    public Map<String, CacheValue<V>> getAll(Set<? extends String> keys) {
        Map<String, CacheValue<Object>> kvs = store.getAllPresent(keys);
        if (Maps.isEmpty(kvs)) {
            return Maps.newHashMap(0);
        }

        Map<String, CacheValue<V>> result = Maps.newHashMap(kvs.size());
        kvs.forEach((key, storeValue) -> {
            CacheValue<V> cacheValue = fromStoreValue(storeValue);
            if (cacheValue != null) {
                result.put(key, cacheValue);
            }
        });

        return result;
    }

    @Override
    public void put(String key, V value) {
        CacheValue<Object> storeValue = toStoreValue(value);
        if (storeValue != null) {
            store.put(key, storeValue);
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        keyValues.forEach(this::put);
    }

    @Override
    public void evict(String key) {
        store.invalidate(key);
    }

    @Override
    public void evictAll(Set<? extends String> keys) {
        store.invalidateAll(keys);
    }

    @Override
    public void clear() {
        // System.out.println("CaffeineStore clear start: " + store.estimatedSize());
        store.invalidateAll();
        // System.out.println("CaffeineStore clear end:" + store.estimatedSize());
    }

}