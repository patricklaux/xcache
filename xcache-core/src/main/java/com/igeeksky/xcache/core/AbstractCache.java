package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.extension.contains.ContainsPredicate;
import com.igeeksky.xcache.extension.lock.KeyLock;
import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.collection.Sets;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-19
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private final String name;
    private final Class<K> keyType;
    private final Class<?>[] keyParams;
    private final Class<V> valueType;
    private final Class<?>[] valueParams;

    private final KeyCodec<K> keyCodec;
    private final CacheStatMonitor statMonitor;

    private final CacheRefresh cacheRefresh;
    private final CacheLoader<K, V> cacheLoader;
    private final LockService lockService;
    private final ContainsPredicate<K> containsPredicate;

    private final String message;

    public AbstractCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend) {
        this.name = config.getName();
        this.keyType = config.getKeyType();
        this.keyParams = config.getKeyParams();
        this.valueType = config.getValueType();
        this.valueParams = config.getValueParams();
        this.message = "Cache:[" + this.name + "], method:[%s], %s";

        this.keyCodec = extend.getKeyCodec();
        this.statMonitor = extend.getStatMonitor();

        this.lockService = extend.getCacheLock();
        this.containsPredicate = extend.getContainsPredicate();
        this.cacheLoader = extend.getCacheLoader();
        this.cacheRefresh = extend.getCacheRefresh();
        if (this.cacheRefresh != null) {
            this.cacheRefresh.setConsumer(this::consume);
        }
    }

    private void consume(String storeKey) {
        K key = this.fromStoreKey(storeKey);
        if (!this.containsPredicate.test(this.name, key)) {
            this.doPut(storeKey, null);
            return;
        }
        KeyLock lock = this.lockService.acquire(storeKey);
        try {
            if (lock.tryLock()) {
                try {
                    V value = this.cacheLoader.load(key);
                    this.doPut(storeKey, value);
                    this.statMonitor.incLoads(value);
                } finally {
                    lock.unlock();
                }
            }
        } finally {
            this.lockService.release(storeKey);
        }
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
        return keyParams;
    }

    @Override
    public Class<V> getValueType() {
        return valueType;
    }

    @Override
    public Class<?>[] getValueParams() {
        return valueParams;
    }

    @Override
    public CacheValue<V> get(K key) {
        requireNonNull(key, "get", "key must not be null.");

        String storeKey = toStoreKey(key);
        recordAccess(storeKey);
        return this.doGet(storeKey);
    }

    protected abstract CacheValue<V> doGet(String key);

    @Override
    public V getOrLoad(K key) {
        requireNonNull(key, "getOrLoad", "key must not be null.");

        return this.doGetOrLoad(key, toStoreKey(key), cacheLoader);
    }

    @Override
    public V get(K key, CacheLoader<K, V> cacheLoader) {
        requireNonNull(key, "get", "key must not be null.");
        requireNonNull(cacheLoader, "get", "cacheLoader must not be null");

        return this.doGetOrLoad(key, toStoreKey(key), cacheLoader);
    }

    private V doGetOrLoad(K key, String storeKey, CacheLoader<K, V> cacheLoader) {
        recordAccess(storeKey);

        CacheValue<V> cacheValue = this.doGet(storeKey);
        if (cacheValue != null) {
            return cacheValue.getValue();
        }

        if (cacheLoader != null) {
            if (!this.containsPredicate.test(this.name, key)) {
                this.doPut(storeKey, null);
                return null;
            }
            return this.load(key, storeKey, cacheLoader);
        }

        return null;
    }

    private V load(K key, String storeKey, CacheLoader<K, V> cacheLoader) {
        KeyLock lock = this.lockService.acquire(storeKey);
        try {
            lock.lock();
            try {
                CacheValue<V> cacheValue = this.doGet(storeKey);
                if (cacheValue != null) {
                    return cacheValue.getValue();
                }
                V value = cacheLoader.load(key);
                this.doPut(storeKey, value);
                this.statMonitor.incLoads(value);
                return value;
            } finally {
                lock.unlock();
            }
        } finally {
            this.lockService.release(storeKey);
        }
    }

    private void recordAccess(String storeKey) {
        if (cacheRefresh != null) {
            cacheRefresh.access(storeKey);
        }
    }

    @Override
    public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
        requireNonNull(keys, "getAll", "keys must not be null.");

        if (keys.isEmpty()) {
            return Maps.newHashMap(0);
        }

        // 1. 建立原生Key 和 缓存Key 之间的映射
        Map<String, K> keyMapping = this.createKeyMapping(keys);

        // 2. 从缓存中获取缓存值
        Map<String, CacheValue<V>> cacheValues = this.doGetAll(keyMapping.keySet());

        // 3. 原生Key 替换 缓存Key，并过滤掉无缓存的结果
        Map<K, CacheValue<V>> result = Maps.newHashMap(cacheValues.size());
        cacheValues.forEach((storeKey, cacheValue) -> {
            if (cacheValue != null) {
                result.put(keyMapping.remove(storeKey), cacheValue);
            }
        });

        return result;
    }

    @Override
    public Map<K, V> getOrLoadAll(Set<? extends K> keys) {
        requireNonNull(keys, "getOrLoadAll", "keys must not be null.");

        return (keys.isEmpty()) ? Maps.newHashMap(0) : this.doGetOrLoadAll(keys, this.cacheLoader);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys, CacheLoader<K, V> cacheLoader) {
        requireNonNull(keys, "getAll", "keys must not be null.");
        requireNonNull(cacheLoader, "getAll", "cacheLoader must not be null.");

        return (keys.isEmpty()) ? Maps.newHashMap(0) : this.doGetOrLoadAll(keys, cacheLoader);
    }

    private Map<K, V> doGetOrLoadAll(Set<? extends K> keys, CacheLoader<K, V> cacheLoader) {
        // 1. 建立原生Key 和 缓存Key 之间的映射
        Map<String, K> keyMapping = this.createKeyMapping(keys);

        // 2. 从缓存中获取缓存值
        Map<String, CacheValue<V>> cacheValues = this.doGetAll(keyMapping.keySet());

        Map<K, V> result = Maps.newHashMap((cacheLoader == null) ? cacheValues.size() : keys.size());

        // 3. 原生Key 替换 缓存Key，并过滤掉无缓存的结果
        cacheValues.forEach((storeKey, cacheValue) -> {
            if (cacheValue != null && cacheValue.hasValue()) {
                result.put(keyMapping.remove(storeKey), cacheValue.getValue());
            }
        });

        if (cacheLoader == null || keyMapping.isEmpty()) {
            return result;
        }

        // 4. 未从缓存中读取到值的 key，使用 cacheLoader 回源取值
        Map<K, V> loads = this.loadAll(keyMapping, cacheLoader);
        Map<String, V> puts = Maps.newHashMap(keyMapping.size());
        keyMapping.forEach((storeKey, key) -> {
            V value = loads.get(key);
            if (value != null) {
                result.put(key, value);
            }
            puts.put(storeKey, value);
        });
        this.doPutAll(puts);

        return result;
    }

    private Map<String, K> createKeyMapping(Set<? extends K> keys) {
        Map<String, K> keyMapping = Maps.newHashMap(keys.size());
        for (K key : keys) {
            requireNonNull(key, "getAll", "keys has null element.");
            keyMapping.put(toStoreKey(key), key);
        }

        if (cacheRefresh != null) {
            cacheRefresh.accessAll(keyMapping.keySet());
        }

        return keyMapping;
    }

    private Map<K, V> loadAll(Map<String, K> keyMapping, CacheLoader<K, V> cacheLoader) {
        Set<K> keys = Sets.newHashSet(keyMapping.size());
        keyMapping.forEach((key1, key) -> {
            if (containsPredicate.test(name, key)) {
                keys.add(key);
            }
        });

        if (keys.isEmpty()) {
            return Maps.newHashMap(0);
        }

        return cacheLoader.loadAll(keys);
    }

    protected abstract Map<String, CacheValue<V>> doGetAll(Set<String> keys);

    @Override
    public void put(K key, V value) {
        requireNonNull(key, "put", "key must not be null.");
        this.doPut(toStoreKey(key), value);
    }

    protected abstract void doPut(String key, V value);

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        requireNonNull(keyValues, "putAll", "keyValues must not be null.");
        if (keyValues.isEmpty()) {
            return;
        }

        Map<String, V> kvs = Maps.newHashMap(keyValues.size());
        keyValues.forEach((key, value) -> {
            requireNonNull(key, "putAll", "keyValues has null element.");
            kvs.put(toStoreKey(key), value);
        });

        this.doPutAll(kvs);
    }

    protected abstract void doPutAll(Map<String, ? extends V> keyValues);

    @Override
    public void evict(K key) {
        requireNonNull(key, "evict", "key must not be null.");

        String storeKey = toStoreKey(key);
        this.doEvict(storeKey);

        if (cacheRefresh != null) {
            cacheRefresh.remove(storeKey);
        }
    }

    protected abstract void doEvict(String key);

    @Override
    public void evictAll(Set<? extends K> keys) {
        requireNonNull(keys, "evictAll", "keys must not be null.");
        if (keys.isEmpty()) {
            return;
        }

        Set<String> set = HashSet.newHashSet(keys.size());
        for (K key : keys) {
            requireNonNull(key, "evictAll", "keys has null element.");
            set.add(toStoreKey(key));
        }

        this.doEvictAll(set);

        if (cacheRefresh != null) {
            cacheRefresh.removeAll(set);
        }
    }

    protected abstract void doEvictAll(Set<String> keys);

    protected String toStoreKey(K key) {
        return keyCodec.encode(key);
    }

    protected K fromStoreKey(String storeKey) {
        return keyCodec.decode(storeKey);
    }

    protected void requireNonNull(Object obj, String method, String tips) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(message, method, tips));
        }
    }

}