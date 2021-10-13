package com.igeeksky.xcache.core;

import com.igeeksky.xcache.AbstractCache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.common.loader.CacheLoader;
import com.igeeksky.xcache.config.MultiCacheProperties;
import com.igeeksky.xcache.extension.contain.ContainsPredicate;
import com.igeeksky.xcache.extension.lock.CacheLock;
import com.igeeksky.xcache.extension.monitor.CacheMonitorProxy;
import com.igeeksky.xcache.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
public abstract class AbstractMultiCache<K, V> extends AbstractCache<K, V> {

    private static final NullOrEmptyKeyException NULL_OR_EMPTY_KEY_EXCEPTION = new NullOrEmptyKeyException();

    private final CacheLock<K> cacheLock;
    private final ContainsPredicate<K> containsPredicate;
    private final CacheMonitorProxy<K, V> cacheMonitor = new CacheMonitorProxy<>();

    public AbstractMultiCache(MultiCacheProperties config, MultiExtension<K, V> extension) {
        super(config.getName(), extension.getKeyType(), extension.getValueType());
        this.cacheLock = extension.getCacheLock();
        this.containsPredicate = extension.getContainsPredicate();
        this.cacheMonitor.addCacheMonitors(extension.getCacheMonitors());
    }

    @Override
    public Mono<CacheValue<V>> get(K key) {
        if (null == key) {
            return Mono.error(NULL_OR_EMPTY_KEY_EXCEPTION);
        }
        return this.doGet(key);
    }

    protected abstract Mono<CacheValue<V>> doGet(K key);

    @Override
    public Mono<CacheValue<V>> get(K key, CacheLoader<K, V> cacheLoader) {
        return this.get(key).switchIfEmpty(loadWithLock(key, cacheLoader));
    }

    private Mono<CacheValue<V>> loadWithLock(K key, CacheLoader<K, V> cacheLoader) {
        if (containsPredicate.test(getName(), key)) {
            Lock keyLock = cacheLock.get(key);
            return Mono.just(key)
                    .doOnNext(k -> keyLock.lock())
                    .flatMap(this::get)
                    .switchIfEmpty(load(key, cacheLoader))
                    .doFinally(s -> keyLock.unlock());
        }
        return Mono.empty();
    }

    private Mono<CacheValue<V>> load(K key, CacheLoader<K, V> cacheLoader) {
        return Mono.fromSupplier(() -> cacheLoader.load(key))
                .doOnNext(value -> cacheMonitor.afterLoad(key, value))
                .doOnNext(value -> this.doPut(key, value))
                .map(CacheValue::new);
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys) {
        if (null == keys) {
            return Flux.error(new NullOrEmptyKeyException("keys must not be null."));
        }
        if (keys.isEmpty()) {
            return Flux.empty();
        }
        return Mono.just(keys)
                .doOnNext(ks -> ks.forEach(key -> {
                    if (null == key) {
                        throw NULL_OR_EMPTY_KEY_EXCEPTION;
                    }
                }))
                .flatMapMany(ks -> this.doGetAll(ks)
                        .doOnNext(kv -> cacheMonitor.afterGet(kv.getKey(), kv.getValue()))
                        .filter(KeyValue::hasValue)
                );
    }

    protected abstract Flux<KeyValue<K, CacheValue<V>>> doGetAll(Set<? extends K> keys);

    @Override
    public Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues) {
        return keyValues
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(kvs -> this.doPutAll(kvs)
                        .doOnNext(vod -> cacheMonitor.afterPutAll(kvs))
                );
    }

    protected abstract Mono<Void> doPutAll(Map<? extends K, ? extends V> keyValues);

    @Override
    public Mono<Void> put(K key, Mono<V> monoValue) {
        if (null == key) {
            return Mono.error(NULL_OR_EMPTY_KEY_EXCEPTION);
        }
        return monoValue
                .flatMap(value -> this.doPut(key, value)
                        .doOnNext(vo -> cacheMonitor.afterPut(key, value))
                );
    }

    protected abstract Mono<Void> doPut(K key, V value);

    @Override
    public Mono<Void> remove(K key) {
        if (null == key) {
            return Mono.error(NULL_OR_EMPTY_KEY_EXCEPTION);
        }
        return this.doRemove(key).doOnNext(v -> cacheMonitor.afterRemove(key));
    }

    protected abstract Mono<Void> doRemove(K key);

    @Override
    public Mono<Void> removeAll(Set<? extends K> keys) {
        if (null == keys) {
            return Mono.error(NULL_OR_EMPTY_KEY_EXCEPTION);
        }
        if (keys.isEmpty()) {
            return Mono.empty();
        }
        return Mono.just(keys)
                .doOnNext(ks -> ks.forEach(key -> {
                    if (null == key) {
                        throw NULL_OR_EMPTY_KEY_EXCEPTION;
                    }
                }))
                .flatMap(ks -> this.doRemoveAll(ks)
                        .doOnNext(v -> cacheMonitor.afterRemoveAll(ks))
                );
    }

    protected abstract Mono<Void> doRemoveAll(Set<? extends K> keys);

    @Override
    public Mono<Void> clear() {
        return this.doClear().doOnNext(vod -> cacheMonitor.afterClear());
    }

    protected abstract Mono<Void> doClear();

}
