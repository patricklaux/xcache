package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.*;
import com.igeeksky.xcache.extension.contains.ContainsPredicate;
import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.collection.Sets;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * 缓存层抽象类
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
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

    private CacheRefresh cacheRefresh;
    private CacheLoader<K, V> cacheLoader;
    private CacheWriter<K, V> cacheWriter;
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
        setCacheWriter(extend.getCacheWriter());
        setCacheRefresh(extend.getCacheRefresh(), extend.getCacheLoader());
    }

    @Override
    public void setCacheLoader(CacheLoader<K, V> cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    @Override
    public void setCacheWriter(CacheWriter<K, V> cacheWriter) {
        this.cacheWriter = new CacheWriterProxy<>(cacheWriter);
    }

    @Override
    public void setCacheRefresh(CacheRefresh cacheRefresh, CacheLoader<K, V> cacheLoader) {
        if (cacheRefresh != null) {
            requireNonNull(cacheLoader, "setCacheRefresh",
                    "Cache refresh depends on the cache loader, cacheLoader must not be null");
        }
        this.cacheLoader = cacheLoader;
        this.cacheRefresh = new CacheRefreshProxy(cacheRefresh, this::consume);
    }

    /**
     * 缓存刷新任务处理
     * <p>
     * 根据传入的 key，回源查询并存入缓存
     *
     * @param storeKey 缓存的键，用于标识缓存项
     */
    private void consume(String storeKey) {
        // 从storeKey中恢复出缓存键
        K key = this.fromStoreKey(storeKey);

        // 如果缓存项不满足预定义的包含条件，则执行放入操作
        if (!this.containsPredicate.test(this.name, key)) {
            this.doPut(storeKey, null);
            return;
        }

        // 获取锁服务以确保并发控制
        Lock lock = this.lockService.acquire(storeKey);
        try {
            // 尝试获取锁，以确保在并发环境下只有一个线程能加载数据
            if (lock.tryLock()) {
                try {
                    // 调用缓存加载器加载数据
                    V value = this.cacheLoader.load(key);
                    // 将加载的数据放入缓存
                    this.doPut(storeKey, value);
                    // 更新统计信息
                    this.statMonitor.incLoads(value);
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            }
        } finally {
            // 释放锁服务
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

        String storeKey = this.toStoreKey(key);
        this.cacheRefresh.access(storeKey);
        return this.doGet(storeKey);
    }

    protected abstract CacheValue<V> doGet(String key);

    @Override
    public V getOrLoad(K key) {
        requireNonNull(key, "getOrLoad", "key must not be null.");

        if (this.cacheLoader == null) {
            CacheValue<V> cacheValue = this.get(key);
            return (cacheValue != null) ? cacheValue.getValue() : null;
        }

        return this.doGetOrLoad(key, this.cacheLoader);
    }

    @Override
    public V get(K key, CacheLoader<K, V> cacheLoader) {
        requireNonNull(key, "get", "key must not be null.");
        requireNonNull(cacheLoader, "get", "cacheLoader must not be null");

        return this.doGetOrLoad(key, cacheLoader);
    }

    private V doGetOrLoad(K key, CacheLoader<K, V> cacheLoader) {
        String storeKey = this.toStoreKey(key);
        this.cacheRefresh.access(storeKey);

        CacheValue<V> cacheValue = this.doGet(storeKey);
        if (cacheValue != null) {
            return cacheValue.getValue();
        }

        if (!this.containsPredicate.test(this.name, key)) {
            this.doPut(storeKey, null);
            return null;
        }

        return this.load(key, storeKey, cacheLoader);
    }

    private V load(K key, String storeKey, CacheLoader<K, V> cacheLoader) {
        Lock lock = this.lockService.acquire(storeKey);
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

        this.cacheRefresh.accessAll(keyMapping.keySet());
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
        this.cacheWriter.write(key, value);
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

        this.cacheWriter.writeAll(keyValues);
        this.doPutAll(kvs);
    }

    protected abstract void doPutAll(Map<String, ? extends V> keyValues);

    @Override
    public void evict(K key) {
        requireNonNull(key, "evict", "key must not be null.");
        String storeKey = toStoreKey(key);

        this.cacheWriter.delete(key);
        this.doEvict(storeKey);
        this.cacheRefresh.remove(storeKey);
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

        this.cacheWriter.deleteAll(keys);
        this.doEvictAll(set);
        this.cacheRefresh.removeAll(set);
    }

    protected abstract void doEvictAll(Set<String> keys);

    protected String toStoreKey(K key) {
        return this.keyCodec.encode(key);
    }

    protected K fromStoreKey(String storeKey) {
        return this.keyCodec.decode(storeKey);
    }

    protected void requireNonNull(Object obj, String method, String tips) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(this.message, method, tips));
        }
    }

}