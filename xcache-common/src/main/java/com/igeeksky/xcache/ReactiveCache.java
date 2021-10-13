package com.igeeksky.xcache;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface ReactiveCache<K, V> {

    Mono<CacheValue<V>> get(K key);

    Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys);

    Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues);

    Mono<Void> put(K key, Mono<V> value);

    Mono<Void> remove(K key);

    Mono<Void> removeAll(Set<? extends K> keys);

    Mono<Void> clear();

}
