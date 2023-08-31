package com.igeeksky.xcache.core;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.config.MultiCacheProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-23
 */
public class OneLevelCache<K, V> extends AbstractMultiCache<K, V> {

    public static final String STORE_TYPE = "one-level-cache";

    private final Cache<K, V> cache;

    public OneLevelCache(MultiCacheProperties cacheConfig, MultiExtension<K, V> multiExtension, Cache<K, V> cache) {
        super(cacheConfig, multiExtension);
        this.cache = cache;
    }

    @Override
    public String getStoreType() {
        return STORE_TYPE;
    }

    @Override
    protected Mono<CacheValue<V>> doGet(K key) {
        return cache.get(key);
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> doGetAll(Set<? extends K> keys) {
        return cache.getAll(keys);
    }

    @Override
    protected Mono<Void> doPut(K key, V value) {
        return cache.put(key, Mono.justOrEmpty(value));
    }

    @Override
    protected Mono<Void> doPutAll(Map<? extends K, ? extends V> keyValues) {
        return cache.putAll(Mono.just(keyValues));
    }

    @Override
    protected Mono<Void> doRemove(K key) {
        return cache.remove(key);
    }

    @Override
    protected Mono<Void> doRemoveAll(Set<? extends K> keys) {
        return Mono.just(keys).flatMap(cache::removeAll);
    }

    @Override
    protected Mono<Void> doClear() {
        return cache.clear();
    }
}
