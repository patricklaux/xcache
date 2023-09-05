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
        return Mono.justOrEmpty(keyValues)
                .filter(Maps::isNotEmpty)
                .map(Map::entrySet)
                .flatMapMany(Flux::fromIterable)
                .filter(Objects::nonNull)
                .doOnNext(entry -> write(entry.getKey(), entry.getValue()))
                .then();
    }

    void delete(K key);

    default Mono<Void> deleteAll(Set<? extends K> keys) {
        return Mono.justOrEmpty(keys)
                .filter(CollectionUtils::isNotEmpty)
                .flatMapMany(Flux::fromIterable)
                .filter(Objects::nonNull)
                .doOnNext(this::delete)
                .then();
    }
}
