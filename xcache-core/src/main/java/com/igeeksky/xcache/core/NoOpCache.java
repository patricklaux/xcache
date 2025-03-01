package com.igeeksky.xcache.core;


import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.ContainsPredicate;
import com.igeeksky.xcache.extension.NoOpCacheLoader;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
    private final Class<V> valueType;
    private final CacheLoader<K, V> cacheLoader;
    private final ContainsPredicate<K> containsPredicate;

    private final String message;

    public NoOpCache(CacheConfig<K, V> config, CacheLoader<K, V> cacheLoader, ContainsPredicate<K> containsPredicate) {
        this.name = config.getName();
        this.keyType = config.getKeyType();
        this.valueType = config.getValueType();
        this.cacheLoader = cacheLoader != null ? cacheLoader : NoOpCacheLoader.getInstance();
        this.containsPredicate = containsPredicate;
        this.message = "Cache:[" + this.name + "], %s";
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
    public Class<V> getValueType() {
        return valueType;
    }

    @Override
    public V get(K key) {
        requireNonNull(key, "key must not be null");
        return null;
    }

    @Override
    public CompletableFuture<V> getAsync(K key) {
        if (key == null) {
            return this.requireNonNull("key must not be null.");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CacheValue<V> getCacheValue(K key) {
        requireNonNull(key, "key must not be null");
        return null;
    }

    @Override
    public CompletableFuture<CacheValue<V>> getCacheValueAsync(K key) {
        if (key == null) {
            return this.requireNonNull("key must not be null.");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public V getOrLoad(K key) {
        return this.getOrLoad(key, this.cacheLoader);
    }

    @Override
    public CompletableFuture<V> getOrLoadAsync(K key) {
        return this.getOrLoadAsync(key, this.cacheLoader);
    }

    @Override
    public V getOrLoad(K key, CacheLoader<K, V> cacheLoader) {
        requireNonNull(key, "key must not be null");
        requireNonNull(cacheLoader, "cacheLoader must not be null");

        if (this.containsPredicate != null) {
            if (this.containsPredicate.test(key)) {
                return cacheLoader.load(key);
            }
            return null;
        }
        return cacheLoader.load(key);
    }

    @Override
    public CompletableFuture<V> getOrLoadAsync(K key, CacheLoader<K, V> cacheLoader) {
        if (key == null) {
            return this.requireNonNull("key must not be null.");
        }
        if (cacheLoader == null) {
            return this.requireNonNull("cacheLoader must not be null.");
        }
        if (this.containsPredicate != null) {
            if (this.containsPredicate.test(key)) {
                return CompletableFuture.completedFuture(key).thenApply(cacheLoader::load);
            }
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(key).thenApply(cacheLoader::load);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        requireNonNull(keys, "keys must not be null");
        return Maps.newHashMap(0);
    }

    @Override
    public CompletableFuture<Map<K, V>> getAllAsync(Set<? extends K> keys) {
        if (keys == null) {
            return this.requireNonNull("keys must not be null.");
        }
        return CompletableFuture.completedFuture(Maps.newHashMap(0));
    }

    @Override
    public Map<K, CacheValue<V>> getAllCacheValues(Set<? extends K> keys) {
        requireNonNull(keys, "keys must not be null");
        return Maps.newHashMap(0);
    }

    @Override
    public CompletableFuture<Map<K, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends K> keys) {
        if (keys == null) {
            return this.requireNonNull("keys must not be null.");
        }
        return CompletableFuture.completedFuture(Maps.newHashMap(0));
    }

    @Override
    public Map<K, V> getAllOrLoad(Set<? extends K> keys) {
        return this.getAllOrLoad(keys, this.cacheLoader);
    }

    @Override
    public CompletableFuture<Map<K, V>> getAllOrLoadAsync(Set<? extends K> keys) {
        return this.getAllOrLoadAsync(keys, this.cacheLoader);
    }

    @Override
    public Map<K, V> getAllOrLoad(Set<? extends K> keys, CacheLoader<K, V> cacheLoader) {
        requireNonNull(keys, "keys must not be null");
        requireNonNull(cacheLoader, "cacheLoader must not be null");
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
    public CompletableFuture<Map<K, V>> getAllOrLoadAsync(Set<? extends K> keys, CacheLoader<K, V> cacheLoader) {
        if (keys == null) {
            return this.requireNonNull("keys must not be null.");
        }
        if (cacheLoader == null) {
            return this.requireNonNull("cacheLoader must not be null.");
        }
        if (this.containsPredicate != null) {
            return CompletableFuture.completedFuture(keys)
                    .thenApply(ks -> {
                        Set<K> exists = HashSet.newHashSet(ks.size());
                        ks.forEach(key -> {
                            if (this.containsPredicate.test(key)) {
                                exists.add(key);
                            }
                        });
                        return cacheLoader.loadAll(exists);
                    });
        }
        return CompletableFuture.completedFuture(keys).thenApply(cacheLoader::loadAll);
    }

    @Override
    public void put(K key, V value) {
        requireNonNull(key, "key must not be null");
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        if (key == null) {
            return this.requireNonNull("key must not be null.");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        requireNonNull(keyValues, "keyValues must not be null");
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> keyValues) {
        if (keyValues == null) {
            return this.requireNonNull("keyValues must not be null.");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void remove(K key) {
        requireNonNull(key, "key must not be null");
    }

    @Override
    public CompletableFuture<Void> removeAsync(K key) {
        if (key == null) {
            return this.requireNonNull("key must not be null.");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        requireNonNull(keys, "keys must not be null");
    }

    @Override
    public CompletableFuture<Void> removeAllAsync(Set<? extends K> keys) {
        if (keys == null) {
            return this.requireNonNull("keys must not be null.");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void clear() {
        // do nothing
    }

    private void requireNonNull(Object obj, String tips) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(message, tips));
        }
    }

    private <T> CompletableFuture<T> requireNonNull(String tips) {
        String msg = String.format(this.message, tips);
        return CompletableFuture.failedFuture(new IllegalArgumentException(msg));
    }

}