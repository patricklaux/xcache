package com.igeeksky.xcache.common.loader;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
@FunctionalInterface
public interface CacheLoader<K, V> {

    V load(K key);

    default Flux<KeyValue<K, CacheValue<V>>> loadAll(Set<? extends K> keys) {
        return Flux.fromIterable(keys)
                .filter(Objects::nonNull)
                .map(key -> {
                    CacheValue<V> cacheValue = new CacheValue<>(load(key));
                    return new KeyValue<>(key, cacheValue);
                });
    }

}
