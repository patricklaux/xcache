package com.igeeksky.xcache.store.no;

import com.igeeksky.xcache.AbstractCache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.common.loader.CacheLoader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-10-04
 */
public class NoOpCacheStoreWithoutMonitor<K, V> extends AbstractCache<K, V> {

    public static final String STORE_TYPE = "no-op-without-monitor";

    public NoOpCacheStoreWithoutMonitor(String name, Class<K> keyType, Class<V> valueType) {
        super(name, keyType, valueType);
    }

    @Override
    public String getStoreType() {
        return STORE_TYPE;
    }

    @Override
    public Mono<CacheValue<V>> get(K key, CacheLoader<K, V> cacheLoader) {
        return Mono.fromSupplier(() -> cacheLoader.load(key)).map(CacheValue::new);
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        return Mono.empty();
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        return Flux.empty();
    }

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> put(K key, Mono<V> value) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> remove(K key) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> removeAll(Set<? extends K> keys) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> clear() {
        return Mono.empty();
    }
}
