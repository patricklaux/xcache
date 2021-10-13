package com.igeeksky.xcache.core;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.config.MultiCacheProperties;
import com.igeeksky.xcache.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * 三级组合缓存
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public class ThreeLevelCache<K, V> extends AbstractMultiCache<K, V> {

    public static final String STORE_TYPE = "three-level-cache";

    private final Cache<K, V> firstCache;
    private final Cache<K, V> secondCache;
    private final Cache<K, V> thirdCache;

    public ThreeLevelCache(MultiCacheProperties cacheConfig, MultiExtension<K, V> multiExtension,
                           Cache<K, V> firstCache, Cache<K, V> secondCache, Cache<K, V> thirdCache) {
        super(cacheConfig, multiExtension);
        this.firstCache = firstCache;
        this.secondCache = secondCache;
        this.thirdCache = thirdCache;
    }

    @Override
    public String getStoreType() {
        return STORE_TYPE;
    }

    @Override
    protected Mono<CacheValue<V>> doGet(K key) {
        return firstCache.get(key)
                .switchIfEmpty(secondCache.get(key)
                        .filter(Objects::nonNull)
                        .doOnNext(cacheValue -> firstCache.put(key, Mono.justOrEmpty(cacheValue.getValue()))))
                .switchIfEmpty(thirdCache.get(key)
                        .filter(Objects::nonNull)
                        .doOnNext(cacheValue -> secondCache.put(key, Mono.justOrEmpty(cacheValue.getValue())))
                        .doOnNext(cacheValue -> firstCache.put(key, Mono.justOrEmpty(cacheValue.getValue())))
                );
    }

    @Override
    protected Flux<KeyValue<K, CacheValue<V>>> doGetAll(Set<? extends K> keys) {
        Set<K> keySet = new HashSet<>(keys);
        return firstCache.getAll(keySet)
                .doOnNext(kv -> keySet.remove(kv.getKey()))
                .collect(() -> new ArrayList<KeyValue<K, CacheValue<V>>>(keySet.size()), ArrayList::add)
                .filter(firstList -> CollectionUtils.isNotEmpty(keySet))
                .flatMap(firstList -> secondCache.getAll(keySet)
                        .doOnNext(kv -> keySet.remove(kv.getKey()))
                        .doOnNext(kv -> firstCache.put(kv.getKey(), Mono.justOrEmpty(kv.getValue().getValue())))
                        .collect(() -> new ArrayList<KeyValue<K, CacheValue<V>>>(keySet.size()), ArrayList::add)
                        .doOnNext(secondList -> secondList.addAll(firstList)))
                .filter(secondList -> CollectionUtils.isNotEmpty(keySet))
                .flatMapMany(secondList -> thirdCache.getAll(keySet)
                        .doOnNext(kv -> secondCache.put(kv.getKey(), Mono.justOrEmpty(kv.getValue().getValue())))
                        .doOnNext(kv -> firstCache.put(kv.getKey(), Mono.justOrEmpty(kv.getValue().getValue())))
                        .concatWith(Flux.fromIterable(secondList))
                );
    }

    @Override
    protected Mono<Void> doPut(K key, V value) {
        return thirdCache.put(key, Mono.justOrEmpty(value))
                .mergeWith(secondCache.put(key, Mono.justOrEmpty(value)))
                .mergeWith(firstCache.put(key, Mono.justOrEmpty(value)))
                .then();
    }

    @Override
    protected Mono<Void> doPutAll(Map<? extends K, ? extends V> keyValues) {
        return thirdCache.putAll(Mono.just(keyValues))
                .mergeWith(secondCache.putAll(Mono.just(keyValues)))
                .mergeWith(firstCache.putAll(Mono.just(keyValues)))
                .then();
    }

    @Override
    protected Mono<Void> doRemove(K key) {
        return thirdCache.remove(key).mergeWith(secondCache.remove(key)).mergeWith(firstCache.remove(key)).then();
    }

    @Override
    protected Mono<Void> doRemoveAll(Set<? extends K> keys) {
        return thirdCache.removeAll(keys)
                .mergeWith(secondCache.removeAll(keys))
                .mergeWith(firstCache.removeAll(keys))
                .then();
    }

    @Override
    protected Mono<Void> doClear() {
        return thirdCache.clear().concatWith(secondCache.clear()).concatWith(firstCache.clear()).then();
    }
}
