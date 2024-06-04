package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xcache.store.AbstractLocalCacheStore;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
public class CaffeineCacheStore<K, V> extends AbstractLocalCacheStore<K, V> {

    public static final String STORE_TYPE = "caffeine";

    private final Cache<K, CacheValue<Object>> cache;

    public CaffeineCacheStore(String name, CacheProperties.Caffeine caffeine,
                              Class<K> keyType, Class<V> valueType, List<CacheMonitor<K, V>> cacheMonitors,
                              Serializer<V> valueSerializer, Compressor compressor,
                              Cache<K, CacheValue<Object>> cache) {
        super(name, caffeine, keyType, valueType, cacheMonitors, valueSerializer, compressor);
        this.cache = cache;
    }

    @Override
    public String getStoreType() {
        return STORE_TYPE;
    }

    @Override
    public Mono<Void> doClear() {
        cache.invalidateAll();
        return Mono.empty();
    }

    @Override
    protected CacheValue<Object> doStoreGet(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    protected void doStorePut(K key, CacheValue<Object> cacheValue) {
        cache.put(key, cacheValue);
    }

    @Override
    protected void doStoreRemove(K key) {
        cache.asMap().remove(key);
    }

    @Override
    protected void doStoreRemoveAll(Set<? extends K> keys) {
        keys.forEach(key -> cache.asMap().remove(key));
    }
}
