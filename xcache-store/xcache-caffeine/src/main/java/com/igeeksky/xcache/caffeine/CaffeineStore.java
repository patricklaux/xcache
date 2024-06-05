package com.igeeksky.xcache.caffeine;


import com.github.benmanes.caffeine.cache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.store.LocalStore;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
public class CaffeineStore implements LocalStore {

    private static final String STORE_NAME = "Caffeine";

    private final Cache<String, CacheValue<Object>> store;

    public CaffeineStore(Cache<String, CacheValue<Object>> store) {
        this.store = store;
    }

    @Override
    public CacheValue<Object> get(String key) {
        return store.getIfPresent(key);
    }

    @Override
    public Map<String, CacheValue<Object>> getAll(Set<? extends String> keys) {
        return store.getAllPresent(keys);
    }

    @Override
    public void doPut(String key, CacheValue<Object> value) {
        store.put(key, value);
    }

    @Override
    public void doPutAll(Map<String, CacheValue<Object>> keyValues) {
        store.putAll(keyValues);
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

    @Override
    public String getStoreName() {
        return STORE_NAME;
    }

}
