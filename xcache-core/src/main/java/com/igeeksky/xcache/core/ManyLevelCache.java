package com.igeeksky.xcache.core;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.config.MultiCacheProperties;
import com.igeeksky.xcache.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 三级组合缓存
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public class ManyLevelCache<K, V> extends AbstractMultiCache<K, V> {

    public static final String STORE_TYPE = "many-level-cache";

    private final List<Cache<K, V>> caches;
    private final List<Cache<K, V>> reverse;

    public ManyLevelCache(MultiCacheProperties cacheConfig, MultiExtension<K, V> multiExtension, List<Cache<K, V>> caches) {
        super(cacheConfig, multiExtension);
        this.caches = caches;
        this.reverse = CollectionUtils.reverse(caches);
    }

    @Override
    public String getStoreType() {
        return STORE_TYPE;
    }

    @Override
    protected Mono<CacheValue<V>> doGet(K key) {
        Mono<CacheValue<V>> mono = Mono.empty();
        int size = caches.size();
        for (int i = 0; i < size; i++) {
            Cache<K, V> cache = caches.get(i);
            int finalI = i;
            mono = mono.switchIfEmpty(cache.get(key)
                    .doOnNext(cacheValue -> {
                        if (null != cacheValue && finalI > 0) {
                            for (int j = finalI - 1; j >= 0; j--) {
                                Cache<K, V> kvCache = reverse.get(j);
                                kvCache.put(key, Mono.justOrEmpty(cacheValue.getValue()));
                            }
                        }
                    })
            );
        }
        return mono;
    }

    @Override
    protected Flux<KeyValue<K, CacheValue<V>>> doGetAll(Set<? extends K> keys) {
        Set<K> keySet = new HashSet<>(keys);
        Flux<KeyValue<K, CacheValue<V>>> flux = Flux.empty();
        for (Cache<K, V> cache : caches) {
            flux = flux.mergeWith(cache.getAll(keySet))
                    .doOnNext(kv -> keySet.remove(kv.getKey()))
                    .filter(kv -> CollectionUtils.isNotEmpty(keySet));
        }
        return flux;
    }

    @Override
    protected Mono<Void> doPut(K key, V value) {
        Flux<Void> flux = Flux.empty();
        for (Cache<K, V> cache : reverse) {
            flux = flux.mergeWith(cache.put(key, Mono.justOrEmpty(value)));
        }
        return flux.then();
    }

    @Override
    protected Mono<Void> doPutAll(Map<? extends K, ? extends V> keyValues) {
        Flux<Void> flux = Flux.empty();
        for (Cache<K, V> cache : reverse) {
            flux = flux.mergeWith(cache.putAll(Mono.just(keyValues)));
        }
        return flux.then();
    }

    @Override
    protected Mono<Void> doRemove(K key) {
        Flux<Void> flux = Flux.empty();
        for (Cache<K, V> cache : reverse) {
            flux = flux.mergeWith(cache.remove(key));
        }
        return flux.then();
    }

    @Override
    protected Mono<Void> doRemoveAll(Set<? extends K> keys) {
        Flux<Void> flux = Flux.empty();
        for (Cache<K, V> cache : reverse) {
            flux = flux.mergeWith(cache.removeAll(keys));
        }
        return flux.then();
    }

    @Override
    protected Mono<Void> doClear() {
        Flux<Void> flux = Flux.empty();
        for (Cache<K, V> cache : reverse) {
            flux.concatWith(cache.clear());
        }
        return flux.then();
    }


}
