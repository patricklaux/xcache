package com.igeeksky.xcache;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

/**
 * 响应式缓存
 *
 * @author Patrick.Lau
 * @date 2021-06-03
 */
public interface ReactiveCache<K, V> {

    /**
     * 传入单个键从缓存中读取值
     *
     * @param key 键
     * @return CacheValue – 值的包装类
     * <p>1. CacheValue 为空，表示 key 不存在于缓存中。</p>
     * <p>2. CacheValue 不为空，表示 key 存在于缓存中：</p>
     * <p>2.1. CacheValue 内部的 value 不为空，缓存的是正常值；</p>
     * <p>2.2. CacheValue 内部的 value 为空，缓存的是空值；</p>
     */
    Mono<CacheValue<V>> get(K key);

    /**
     * 传入多个键从缓存中读取值
     *
     * @param keys 多个键的集合
     * @return KeyValue – 键值对的包装类
     * <p>1. CacheValue 为空，表示 key 不存在于缓存中。</p>
     * <p>2. CacheValue 不为空，表示 key 存在于缓存中：</p>
     * <p>2.1. CacheValue 内部的 value 不为空，缓存的是正常值；</p>
     * <p>2.2. CacheValue 内部的 value 为空，缓存的是空值；</p>
     */
    Flux<KeyValue<K, CacheValue<V>>> getAll(Set<? extends K> keys);

    /**
     * 将多个键值对存入到缓存中
     *
     * @param keyValues 键值对的集合
     * @return 空
     */
    Mono<Void> putAll(Mono<Map<? extends K, ? extends V>> keyValues);

    Mono<Void> put(K key, Mono<V> value);

    Mono<Void> remove(K key);

    Mono<Void> removeAll(Set<? extends K> keys);

    Mono<Void> clear();

}
