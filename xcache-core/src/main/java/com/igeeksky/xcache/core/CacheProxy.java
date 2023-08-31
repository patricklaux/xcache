package com.igeeksky.xcache.core;


import com.igeeksky.xcache.AbstractCache;
import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.common.loader.CacheLoader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * 缓存代理类<br/>
 * 用于在运行时变更缓存实例
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-19
 */
public class CacheProxy<K, V> extends AbstractCache<K, V> {

    private volatile Cache<K, V> cache;

    public CacheProxy(Cache<K, V> cache) {
        super(cache.getName(), cache.getKeyType(), cache.getValueType());
        this.cache = cache;
    }

    @SuppressWarnings("unchecked")
    void setCache(Cache<?, ?> cache) {
        this.cache = (Cache<K, V>) cache;
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Class<K> getKeyType() {
        return cache.getKeyType();
    }

    @Override
    public Class<V> getValueType() {
        return cache.getValueType();
    }

    @Override
    public String getStoreType() {
        return "cache-proxy";
    }

    @Override
    public Mono<CacheValue<V>> get(K key, CacheLoader<K, V> cacheLoader) {
        return cache.get(key, cacheLoader);
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        return cache.get(key);
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        return cache.getAll(keys);
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return cache.putAll(keyValues);
    }

    @Override
    public Mono<Void> put(K key, Mono<V> value) {
        return cache.put(key, value);
    }

    @Override
    public Mono<Void> remove(K key) {
        return cache.remove(key);
    }

    @Override
    public Mono<Void> removeAll(Set<? extends K> keys) {
        return cache.removeAll(keys);
    }

    @Override
    public Mono<Void> clear() {
        return cache.clear();
    }
}
