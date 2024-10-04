package com.igeeksky.xcache.caffeine;


import com.github.benmanes.caffeine.cache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.EmbedStoreValueConvertor;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.Map;
import java.util.Set;

/**
 * Caffeine 作为存储层
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
public class CaffeineStore<V> implements Store<V> {

    private final Cache<String, CacheValue<Object>> store;
    private final EmbedStoreValueConvertor<V> convertor;

    public CaffeineStore(Cache<String, CacheValue<Object>> store, CaffeineConfig<V> config) {
        this.store = store;
        this.convertor = new EmbedStoreValueConvertor<>(config.isEnableNullValue(),
                config.isEnableCompressValue(), config.isEnableSerializeValue(),
                config.getValueCodec(), config.getValueCompressor());
    }

    @Override
    public CacheValue<V> get(String key) {
        return this.convertor.fromStoreValue(store.getIfPresent(key));
    }

    @Override
    public Map<String, CacheValue<V>> getAll(Set<? extends String> keys) {
        Map<String, CacheValue<Object>> kvs = store.getAllPresent(keys);
        if (Maps.isEmpty(kvs)) {
            return Maps.newHashMap(0);
        }

        Map<String, CacheValue<V>> result = Maps.newHashMap(kvs.size());
        kvs.forEach((key, storeValue) -> {
            CacheValue<V> cacheValue = this.convertor.fromStoreValue(storeValue);
            if (cacheValue != null) {
                result.put(key, cacheValue);
            }
        });

        return result;
    }

    @Override
    public void put(String key, V value) {
        CacheValue<Object> storeValue = this.convertor.toStoreValue(value);
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
        store.invalidateAll();
    }

}