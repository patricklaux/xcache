package com.igeeksky.xcache.core;


import com.igeeksky.xtool.core.collection.Maps;

import java.util.Map;
import java.util.Set;

/**
 * 无操作缓存
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-19
 */
public class NoOpCache<K, V> implements Cache<K, V> {

    private final String name;
    private final Class<K> keyType;
    private final Class<?>[] keyParams;
    private final Class<V> valueType;
    private final Class<?>[] valueParams;
    private final CacheLoader<K, V> loader;

    private final String message;

    public NoOpCache(CacheConfig<K, V> config, CacheLoader<K, V> loader) {
        this.name = config.getName();
        this.keyType = config.getKeyType();
        this.keyParams = config.getKeyParams();
        this.valueType = config.getValueType();
        this.valueParams = config.getValueParams();
        this.loader = loader;
        this.message = "Cache:[" + this.name + "], method:[%s], %s";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<K> getKeyType() {
        return keyType;
    }

    @Override
    public Class<?>[] getKeyParams() {
        return this.keyParams;
    }

    @Override
    public Class<?>[] getValueParams() {
        return this.valueParams;
    }

    @Override
    public V getOrLoad(K key) {
        requireNonNull(key, "getOrLoad", "key must not be null");
        if (loader != null) {
            return loader.load(key);
        }
        return null;
    }

    @Override
    public Class<V> getValueType() {
        return valueType;
    }

    @Override
    public CacheValue<V> get(K key) {
        requireNonNull(key, "get", "key must not be null");
        return null;
    }

    @Override
    public V get(K key, CacheLoader<K, V> cacheLoader) {
        requireNonNull(key, "get", "key must not be null");
        requireNonNull(cacheLoader, "get", "cacheLoader must not be null");
        return cacheLoader.load(key);
    }

    @Override
    public Map<K, V> getOrLoadAll(Set<? extends K> keys) {
        requireNonNull(keys, "getOrLoadAll", "keys must not be null");
        if (loader != null) {
            return loader.loadAll(keys);
        }
        return Maps.newHashMap(0);
    }

    @Override
    public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
        requireNonNull(keys, "getAll", "keys must not be null");
        return Maps.newHashMap(0);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys, CacheLoader<K, V> cacheLoader) {
        requireNonNull(keys, "getAll", "keys must not be null");
        requireNonNull(cacheLoader, "getAll", "cacheLoader must not be null");
        return cacheLoader.loadAll(keys);
    }

    @Override
    public void put(K key, V value) {
        requireNonNull(key, "put", "key must not be null");
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        requireNonNull(keyValues, "putAll", "keyValues must not be null");
    }

    @Override
    public void evict(K key) {
        requireNonNull(key, "evict", "key must not be null");
    }

    @Override
    public void evictAll(Set<? extends K> keys) {
        requireNonNull(keys, "evictAll", "keys must not be null");
    }

    @Override
    public void clear() {
    }

    protected void requireNonNull(Object obj, String method, String tips) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(message, method, tips));
        }
    }
}