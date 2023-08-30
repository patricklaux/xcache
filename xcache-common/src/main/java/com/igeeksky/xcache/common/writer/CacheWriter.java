package com.igeeksky.xcache.common.writer;


import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
public interface CacheWriter<K, V> {

    void write(K key, V value);

    default Mono<Void> writeAll(Map<? extends K, ? extends V> keyValues) {
        if (Maps.isNotEmpty(keyValues)) {
            return Flux.fromIterable(keyValues.entrySet())
                    .filter(Objects::nonNull)
                    .doOnNext(entry -> write(entry.getKey(), entry.getValue()))
                    .then();
        }
        return Mono.empty();
    }

    void delete(K key);

    default Mono<Void> deleteAll(Set<? extends K> keys) {
        if (CollectionUtils.isNotEmpty(keys)) {
            return Flux.fromIterable(keys)
                    .filter(Objects::nonNull)
                    .doOnNext(this::delete)
                    .then();
        }
        return Mono.empty();
    }
}
