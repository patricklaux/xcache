package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.igeeksky.xcache.common.ExpiryCacheValue;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xcache.store.AbstractExpiryLocalCacheStore;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
public class CaffeineExpiryCacheStore<K, V> extends AbstractExpiryLocalCacheStore<K, V> {

    public static final String STORE_TYPE = "caffeine";

    private final Cache<K, ExpiryCacheValue<Object>> cache;

    public CaffeineExpiryCacheStore(String name, CacheProperties.Caffeine caffeine,
                                    Class<K> keyType, Class<V> valueType, List<CacheMonitor<K, V>> cacheMonitors,
                                    Serializer<V> valueSerializer, Compressor compressor,
                                    Cache<K, ExpiryCacheValue<Object>> cache) {
        super(name, caffeine, keyType, valueType, cacheMonitors, valueSerializer, compressor);
        this.cache = cache;
    }

    @Override
    public String getStoreType() {
        return STORE_TYPE;
    }

    @Override
    public Mono<Void> doClear() {
        cache.asMap().clear();
        return Mono.empty();
    }

    @Override
    protected ExpiryCacheValue<Object> doStoreGet(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    protected void doStorePut(K key, ExpiryCacheValue<Object> cacheValue) {
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
