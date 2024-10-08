package com.igeeksky.xcache.core;


import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheWriter;
import com.igeeksky.xcache.extension.NoopCacheLoader;
import com.igeeksky.xcache.extension.NoopCacheWriter;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.Map;
import java.util.Set;

/**
 * 无操作缓存
 * <p>
 * 当配置禁用缓存时，将使用此缓存实现类，避免改动代码
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-19
 */
public class NoopCache<K, V> implements Cache<K, V> {

    private final String name;
    private final Class<K> keyType;
    private final Class<?>[] keyParams;
    private final Class<V> valueType;
    private final Class<?>[] valueParams;
    private final CacheLoader<K, V> cacheLoader;
    private final CacheWriter<K, V> cacheWriter;

    private final String message;

    public NoopCache(CacheConfig<K, V> config, CacheLoader<K, V> cacheLoader, CacheWriter<K, V> cacheWriter) {
        this.name = config.getName();
        this.keyType = config.getKeyType();
        this.keyParams = config.getKeyParams();
        this.valueType = config.getValueType();
        this.valueParams = config.getValueParams();
        this.cacheLoader = cacheLoader != null ? cacheLoader : NoopCacheLoader.getInstance();
        this.cacheWriter = cacheWriter != null ? cacheWriter : NoopCacheWriter.getInstance();
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
    public Class<V> getValueType() {
        return valueType;
    }

    @Override
    public CacheValue<V> get(K key) {
        requireNonNull(key, "get", "key must not be null");
        return null;
    }

    @Override
    public V getOrLoad(K key) {
        requireNonNull(key, "getOrLoad", "key must not be null");
        return this.cacheLoader.load(key);
    }

    @Override
    public V get(K key, CacheLoader<K, V> cacheLoader) {
        requireNonNull(key, "get", "key must not be null");
        requireNonNull(cacheLoader, "get", "cacheLoader must not be null");
        return cacheLoader.load(key);
    }

    @Override
    public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
        requireNonNull(keys, "getAll", "keys must not be null");
        return Maps.newHashMap(0);
    }

    @Override
    public Map<K, V> getOrLoadAll(Set<? extends K> keys) {
        requireNonNull(keys, "getOrLoadAll", "keys must not be null");
        return this.cacheLoader.loadAll(keys);
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
        this.cacheWriter.write(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        requireNonNull(keyValues, "putAll", "keyValues must not be null");
        this.cacheWriter.writeAll(keyValues);
    }

    @Override
    public void evict(K key) {
        requireNonNull(key, "evict", "key must not be null");
        this.cacheWriter.delete(key);
    }

    @Override
    public void evictAll(Set<? extends K> keys) {
        requireNonNull(keys, "evictAll", "keys must not be null");
        this.cacheWriter.deleteAll(keys);
    }

    @Override
    public void clear() {
    }

    private void requireNonNull(Object obj, String method, String tips) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(message, method, tips));
        }
    }

}