package com.igeeksky.xcache.core;


import com.igeeksky.xcache.common.*;
import com.igeeksky.xcache.extension.NoOpCacheLoader;
import com.igeeksky.xcache.extension.NoOpCacheWriter;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.HashSet;
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
public class NoOpCache<K, V> implements Cache<K, V> {

    private final String name;
    private final Class<K> keyType;
    private final Class<?>[] keyParams;
    private final Class<V> valueType;
    private final Class<?>[] valueParams;
    private final CacheLoader<K, V> cacheLoader;
    private final CacheWriter<K, V> cacheWriter;
    private final ContainsPredicate<K> containsPredicate;

    private final String message;

    public NoOpCache(CacheConfig<K, V> config, CacheLoader<K, V> cacheLoader,
                     CacheWriter<K, V> cacheWriter, ContainsPredicate<K> containsPredicate) {
        this.name = config.getName();
        this.keyType = config.getKeyType();
        this.keyParams = config.getKeyParams();
        this.valueType = config.getValueType();
        this.valueParams = config.getValueParams();
        this.cacheLoader = cacheLoader != null ? cacheLoader : NoOpCacheLoader.getInstance();
        this.cacheWriter = cacheWriter != null ? cacheWriter : NoOpCacheWriter.getInstance();
        this.containsPredicate = containsPredicate;
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
    public Class<V> getValueType() {
        return valueType;
    }

    @Override
    public Class<?>[] getValueParams() {
        return this.valueParams;
    }

    @Override
    public V get(K key) {
        requireNonNull(key, "get", "key must not be null");
        return null;
    }

    @Override
    public CacheValue<V> getCacheValue(K key) {
        requireNonNull(key, "get", "key must not be null");
        return null;
    }

    @Override
    public V getOrLoad(K key) {
        return this.getOrLoad(key, this.cacheLoader);
    }

    @Override
    public V getOrLoad(K key, CacheLoader<K, V> cacheLoader) {
        requireNonNull(key, "getOrLoad", "key must not be null");
        requireNonNull(cacheLoader, "getOrLoad", "cacheLoader must not be null");

        if (this.containsPredicate != null) {
            if (this.containsPredicate.test(key)) {
                return cacheLoader.load(key);
            }
            return null;
        }
        return cacheLoader.load(key);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        requireNonNull(keys, "getAll", "keys must not be null");
        return Maps.newHashMap(0);
    }

    @Override
    public Map<K, CacheValue<V>> getAllCacheValues(Set<? extends K> keys) {
        requireNonNull(keys, "getAll", "keys must not be null");
        return Maps.newHashMap(0);
    }

    @Override
    public Map<K, V> getAllOrLoad(Set<? extends K> keys) {
        return this.getAllOrLoad(keys, this.cacheLoader);
    }

    @Override
    public Map<K, V> getAllOrLoad(Set<? extends K> keys, CacheLoader<K, V> cacheLoader) {
        requireNonNull(keys, "getAllOrLoad", "keys must not be null");
        requireNonNull(cacheLoader, "getAllOrLoad", "cacheLoader must not be null");
        if (this.containsPredicate != null) {
            Set<K> exists = HashSet.newHashSet(keys.size());
            keys.forEach(key -> {
                if (this.containsPredicate.test(key)) {
                    exists.add(key);
                }
            });
            return cacheLoader.loadAll(exists);
        }
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
    public void remove(K key) {
        requireNonNull(key, "evict", "key must not be null");
        this.cacheWriter.delete(key);
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        requireNonNull(keys, "evictAll", "keys must not be null");
        this.cacheWriter.deleteAll(keys);
    }

    @Override
    public void clear() {
        // do nothing
    }

    private void requireNonNull(Object obj, String method, String tips) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(message, method, tips));
        }
    }

}