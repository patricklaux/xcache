package com.igeeksky.xcache.common.loader;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return Mono.justOrEmpty(keys)
                .filter(CollectionUtils::isNotEmpty)
                .flatMapMany(Flux::fromIterable)
                .filter(Objects::nonNull)
                .map(key -> new KeyValue<>(key, new CacheValue<>(load(key))));
    }

}
