package com.igeeksky.xcache.store;


import com.igeeksky.xcache.AbstractCache;
import com.igeeksky.xcache.common.CacheLevel;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.common.loader.CacheLoader;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.monitor.CacheMonitorProxy;
import com.igeeksky.xtool.core.collection.Maps;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-29
 */
public abstract class AbstractCacheStore<K, V> extends AbstractCache<K, V> {

    protected final CacheLevel cacheLevel;
    protected final boolean enableNullValue;

    private final CacheMonitorProxy<K, V> cacheMonitor = new CacheMonitorProxy<>();

    public AbstractCacheStore(String name, CacheProperties.Generic generic, Class<K> keyType, Class<V> valueType,
                              List<CacheMonitor<K, V>> cacheMonitors) {
        super(name, keyType, valueType);
        this.cacheLevel = generic.getCacheLevel();
        this.enableNullValue = generic.isEnableNullValue();
        this.cacheMonitor.addCacheMonitors(cacheMonitors);
    }

    public CacheLevel getCacheLevel() {
        return cacheLevel;
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        return doGet(key).doOnNext(cacheValue -> cacheMonitor.afterGet(key, cacheValue));
    }

    protected abstract Mono<CacheValue<V>> doGet(K key);

    @Override
    public Mono<CacheValue<V>> get(K key, CacheLoader<K, V> cacheLoader) {
        return get(key)
                .switchIfEmpty(Mono.fromSupplier(() -> cacheLoader.load(key))
                        .doOnNext(value -> cacheMonitor.afterLoad(key, value))
                        .doOnNext(value -> doPut(key, value))
                        .map(CacheValue::new)
                );
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        return doGetAll(keys)
                .collect(() -> new ArrayList<KeyValue<K, CacheValue<V>>>(keys.size()), ArrayList::add)
                .doOnNext(cacheMonitor::afterGetAll)
                .flatMapMany(Flux::fromIterable)
                .filter(KeyValue::hasValue);
    }

    protected abstract Flux<KeyValue<K, CacheValue<V>>> doGetAll(Set<? extends K> keys);

    @Override
    public Mono<Void> put(K key, Mono<V> monoValue) {
        return monoValue.filter(value -> enableNullValue || null != value)
                .flatMap(value -> doPut(key, value).doOnNext(vo -> cacheMonitor.afterPut(key, value)));
    }

    protected abstract Mono<Void> doPut(K key, V value);

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return keyValues.map(kvs -> {
                    Map<K, V> map = new HashMap<>(kvs.size());
                    Set<? extends Map.Entry<? extends K, ? extends V>> entrySet = kvs.entrySet();
                    for (Map.Entry<? extends K, ? extends V> entry : entrySet) {
                        K key = entry.getKey();
                        V value = entry.getValue();
                        if (null == value && !enableNullValue) {
                            continue;
                        }
                        map.put(key, value);
                    }
                    return map;
                })
                .filter(Maps::isNotEmpty)
                .flatMap(map -> doPutAll(map).doOnNext(vo -> cacheMonitor.afterPutAll(map)));
    }

    protected abstract Mono<Void> doPutAll(Map<? extends K, ? extends V> keyValues);

    @Override
    public Mono<Void> remove(K key) {
        return doRemove(key).doOnNext(v -> cacheMonitor.afterRemove(key));
    }

    protected abstract Mono<Void> doRemove(K key);

    @Override
    public Mono<Void> removeAll(Set<? extends K> keys) {
        return doRemoveAll(keys).doOnNext(vo -> cacheMonitor.afterRemoveAll(keys));
    }

    protected abstract Mono<Void> doRemoveAll(Set<? extends K> keys);

    @Override
    public Mono<Void> clear() {
        return doClear().doOnNext(vod -> cacheMonitor.afterClear());
    }

    protected abstract Mono<Void> doClear();

}
