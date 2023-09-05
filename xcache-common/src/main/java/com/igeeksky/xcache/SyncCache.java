package com.igeeksky.xcache;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.common.loader.CacheLoader;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 同步缓存
 *
 * @author Patrick.Lau
 * @since 0.0.3 2020-12-11
 */
public interface SyncCache<K, V> {

    /**
     * 通过键从缓存中读取值
     *
     * @param key 键
     * @return CacheValue – 值的包装类
     * <p>1. CacheValue 为空，表示 key 不存在于缓存中。</p>
     * <p>2. CacheValue 不为空，表示 key 存在于缓存中：</p>
     * <p>2.1. CacheValue 内部的 value 不为空，缓存的是正常值；</p>
     * <p>2.2. CacheValue 内部的 value 为空，缓存的是空值；</p>
     */
    CacheValue<V> get(K key);

    CacheValue<V> get(K key, CacheLoader<K, V> loader);

    Map<K, CacheValue<V>> getAll(Set<? extends K> keys);

    void put(K key, V value);

    void putAll(Map<? extends K, ? extends V> map);

    void remove(K key);

    void removeAll(Set<? extends K> keys);

    void clear();

    class SyncCacheView<K, V> implements SyncCache<K, V> {

        private final Cache<K, V> cache;

        public SyncCacheView(Cache<K, V> cache) {
            this.cache = cache;
        }

        @Override
        public CacheValue<V> get(K key) {
            return cache.get(key).block();
        }

        @Override
        public CacheValue<V> get(K key, CacheLoader<K, V> loader) {
            return cache.get(key, loader).block();
        }

        @Override
        public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
            return cache.getAll(keys)
                    .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue, (a, b) -> b))
                    .block();
        }

        @Override
        public void put(K key, V value) {
            cache.put(key, Mono.justOrEmpty(value)).block();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            cache.putAll(Mono.justOrEmpty(map)).block();
        }

        @Override
        public void remove(K key) {
            cache.remove(key).block();
        }

        @Override
        public void removeAll(Set<? extends K> keys) {
            cache.removeAll(keys).block();
        }

        @Override
        public void clear() {
            cache.clear().block();
        }
    }

}
