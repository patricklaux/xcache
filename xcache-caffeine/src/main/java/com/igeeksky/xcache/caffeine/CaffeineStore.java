package com.igeeksky.xcache.caffeine;


import com.github.benmanes.caffeine.cache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.EmbedStoreValueConvertor;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
    public CacheValue<V> getCacheValue(String key) {
        CacheValue<Object> storeValue = this.store.getIfPresent(key);
        if (storeValue != null) {
            return this.convertor.fromStoreValue(storeValue);
        }
        return null;
    }

    @Override
    public CompletableFuture<CacheValue<V>> getCacheValueAsync(String key) {
        return CompletableFuture.completedFuture(key).thenApply(this::getCacheValue);
    }

    @Override
    public Map<String, CacheValue<V>> getAllCacheValues(Set<? extends String> keys) {
        Map<String, CacheValue<Object>> keyValues = store.getAllPresent(keys);
        if (Maps.isEmpty(keyValues)) {
            return Collections.emptyMap();
        }
        Map<String, CacheValue<V>> result = HashMap.newHashMap(keyValues.size());
        keyValues.forEach((key, value) -> {
            CacheValue<V> cacheValue = this.convertor.fromStoreValue(value);
            if (cacheValue != null) {
                result.put(key, cacheValue);
            }
        });
        return result;
    }

    @Override
    public CompletableFuture<Map<String, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends String> keys) {
        return CompletableFuture.completedFuture(keys).thenApply(this::getAllCacheValues);
    }

    @Override
    public void put(String key, V value) {
        CacheValue<Object> storeValue = this.convertor.toStoreValue(value);
        if (storeValue != null) {
            store.put(key, storeValue);
        } else {
            store.invalidate(key);
        }
    }

    @Override
    public CompletableFuture<Void> putAsync(String key, V value) {
        return CompletableFuture.completedFuture(KeyValue.create(key, value))
                .thenAccept(kv -> this.put(kv.getKey(), kv.getValue()));
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        keyValues.forEach(this::put);
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends String, ? extends V> keyValues) {
        return CompletableFuture.completedFuture(keyValues).thenAccept(this::putAll);
    }

    @Override
    public void remove(String key) {
        store.invalidate(key);
    }

    @Override
    public CompletableFuture<Void> removeAsync(String key) {
        return CompletableFuture.completedFuture(key).thenAccept(this::remove);
    }

    @Override
    public void removeAll(Set<? extends String> keys) {
        store.invalidateAll(keys);
    }

    @Override
    public CompletableFuture<Void> removeAllAsync(Set<? extends String> keys) {
        return CompletableFuture.completedFuture(keys).thenAccept((this::removeAll));
    }

    @Override
    public void clear() {
        store.invalidateAll();
    }

}