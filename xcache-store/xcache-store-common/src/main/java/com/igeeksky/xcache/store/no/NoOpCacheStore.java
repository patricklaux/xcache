package com.igeeksky.xcache.store.no;


import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.store.AbstractCacheStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 无操作缓存
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-19
 */
public class NoOpCacheStore<K, V> extends AbstractCacheStore<K, V> {

    public static final String STORE_TYPE = "no-op";

    public NoOpCacheStore(String name, CacheProperties.Generic generic, Class<K> keyType, Class<V> valueType,
                          List<CacheMonitor<K, V>> cacheMonitors) {
        super(name, generic, keyType, valueType, cacheMonitors);
    }

    @Override
    public String getStoreType() {
        return STORE_TYPE;
    }

    @Override
    protected Mono<CacheValue<V>> doGet(K key) {
        return Mono.empty();
    }

    @Override
    protected Flux<KeyValue<K, CacheValue<V>>> doGetAll(Set<? extends K> keys) {
        return Flux.empty();
    }

    @Override
    protected Mono<Void> doPut(K key, V value) {
        return Mono.empty();
    }

    @Override
    protected Mono<Void> doPutAll(Map<? extends K, ? extends V> keyValues) {
        return Mono.empty();
    }

    @Override
    protected Mono<Void> doRemove(K key) {
        return Mono.empty();
    }

    @Override
    protected Mono<Void> doRemoveAll(Set<? extends K> keys) {
        return Mono.empty();
    }

    @Override
    protected Mono<Void> doClear() {
        return Mono.empty();
    }
}
