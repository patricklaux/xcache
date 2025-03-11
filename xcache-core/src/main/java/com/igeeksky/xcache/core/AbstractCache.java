package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.ContainsPredicate;
import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xcache.extension.metrics.CacheMetricsMonitor;
import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.refresh.NoOpCacheRefresh;
import com.igeeksky.xtool.core.collection.Sets;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
    private final Class<V> valueType;

    private final KeyCodec<K> keyCodec;
    private final CacheMetricsMonitor metricsMonitor;

    private CacheRefresh cacheRefresh;
    private CacheLoader<K, V> cacheLoader;
    private final LockService lockService;
    private final ContainsPredicate<K> containsPredicate;

    private final String message;

    public AbstractCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend) {
        this.name = config.getName();
        this.keyType = config.getKeyType();
        this.valueType = config.getValueType();
        this.message = "Cache:[" + this.name + "], %s";

        this.keyCodec = extend.getKeyCodec();
        this.metricsMonitor = extend.getMetricsMonitor();

        this.lockService = extend.getLockService();
        this.containsPredicate = extend.getContainsPredicate();
        this.setCacheRefresh(extend.getCacheRefresh(), extend.getCacheLoader());
    }

    private void setCacheRefresh(CacheRefresh cacheRefresh, CacheLoader<K, V> cacheLoader) {
        if (cacheRefresh != null) {
            requireNonNull(cacheLoader, "Cache refresh depends on the cache loader, cacheLoader must not be null");
        }
        // 此抽象类的 cacheLoader 在 getOrLoad 和 getOrLoadAll 方法中需要根据是否为空来判断是否加锁数据回源
        // 因此当 cacheLoader 为空时，不能赋值 为 NoopCacheLoader
        this.cacheLoader = cacheLoader;
        this.cacheRefresh = cacheRefresh != null ? cacheRefresh : NoOpCacheRefresh.getInstance();
        this.cacheRefresh.startRefresh(this::refresh, this::contains);
    }

    /**
     * 缓存刷新任务处理
     * <p>
     * 根据传入的 key，回源查询并存入缓存
     *
     * @param storeKey 缓存键
     */
    private void refresh(String storeKey) {
        // 从storeKey中恢复缓存键
        K key = this.fromStoreKey(storeKey);

        // 如果断言执行发现数据源不存在数据，则存入空值
        if (!this.containsPredicate.test(key)) {
            this.doPutAsync(storeKey, null);
            return;
        }

        // 获取锁服务以确保并发控制
        Lock lock = this.lockService.acquire(storeKey);
        try {
            // 尝试获取锁，以确保在并发环境下只有一个线程能加载数据
            if (lock.tryLock()) {
                try {
                    // 调用缓存加载器加载数据，并将结果存入缓存，并记录统计信息
                    V value = this.cacheLoader.load(key);
                    this.doPutAsync(storeKey, value);
                    if (value != null) {
                        this.metricsMonitor.incHitLoads(1);
                    } else {
                        this.metricsMonitor.incMissLoads(1);
                    }
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

    /**
     * 最后一级缓存是否存在键对应的值
     *
     * @param storeKey 缓存键
     * @return {@code true} 存在； {@code false} 不存在
     */
    protected abstract boolean contains(String storeKey);

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
        CacheValue<V> cacheValue = this.getCacheValue(key);
        return (cacheValue != null) ? cacheValue.getValue() : null;
    }

    @Override
    public CompletableFuture<V> getAsync(K key) {
        return this.getCacheValueAsync(key)
                .thenApply(cacheValue -> (cacheValue != null) ? cacheValue.getValue() : null);
    }

    @Override
    public CacheValue<V> getCacheValue(K key) {
        requireNonNull(key, "key must not be null.");
        return this.doGet(this.toStoreKey(key));
    }

    @Override
    public CompletableFuture<CacheValue<V>> getCacheValueAsync(K key) {
        if (key == null) {
            return this.requireNonNull("key must not be null.");
        }
        return CompletableFuture.completedFuture(this.toStoreKey(key)).thenCompose(this::doGetAsync);
    }

    @Override
    public V getOrLoad(K key) {
        if (this.cacheLoader == null) {
            return this.get(key);
        }
        return this.getOrLoad(key, this.cacheLoader);
    }

    @Override
    public CompletableFuture<V> getOrLoadAsync(K key) {
        if (this.cacheLoader == null) {
            return this.getAsync(key);
        }
        return this.getOrLoadAsync(key, this.cacheLoader);
    }

    @Override
    public V getOrLoad(K key, CacheLoader<K, V> cacheLoader) {
        requireNonNull(key, "key must not be null.");
        requireNonNull(cacheLoader, "cacheLoader must not be null");
        String storeKey = this.toStoreKey(key);
        CacheValue<V> cacheValue = this.doGet(storeKey);
        if (cacheValue != null) {
            return cacheValue.getValue();
        }
        if (this.containsPredicate.test(key)) {
            return this.load(key, storeKey, cacheLoader);
        }
        return null;
    }

    @Override
    public CompletableFuture<V> getOrLoadAsync(K key, CacheLoader<K, V> cacheLoader) {
        if (key == null) {
            return this.requireNonNull("key must not be null.");
        }
        if (cacheLoader == null) {
            return this.requireNonNull("cacheLoader must not be null.");
        }
        return CompletableFuture.completedFuture(this.toStoreKey(key))
                .thenCompose(storeKey -> this.doGetAsync(storeKey)
                        .thenApply(cacheValue -> {
                            if (cacheValue != null) {
                                return cacheValue.getValue();
                            }
                            if (this.containsPredicate.test(key)) {
                                return this.load(key, storeKey, cacheLoader);
                            }
                            return null;
                        })
                );
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
                this.doPutAndRefresh(storeKey, value);
                return value;
            } finally {
                lock.unlock();
            }
        } finally {
            this.lockService.release(storeKey);
        }
    }

    @Override
    public Map<K, CacheValue<V>> getAllCacheValues(Set<? extends K> keys) {
        requireNonNull(keys, "keys must not be null.");
        if (keys.isEmpty()) {
            return HashMap.newHashMap(0);
        }
        Map<String, K> keyMapping = this.createKeyMapping(keys);
        return this.saveToWrapperResult(keyMapping, this.doGetAll(keyMapping.keySet()));
    }

    @Override
    public CompletableFuture<Map<K, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends K> keys) {
        if (keys == null) {
            return requireNonNull("keys must not be null.");
        }
        if (keys.isEmpty()) {
            return CompletableFuture.completedFuture(HashMap.newHashMap(0));
        }
        return CompletableFuture.completedFuture(keys)
                .thenApply(this::createKeyMapping)
                .thenCompose(keyMapping -> {
                    Set<String> keySet = keyMapping.keySet();
                    return this.doGetAllAsync(keySet)
                            .thenApply(cacheValues -> this.saveToWrapperResult(keyMapping, cacheValues));
                });
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        requireNonNull(keys, "keys must not be null.");
        if (keys.isEmpty()) {
            return HashMap.newHashMap(0);
        }
        Map<String, K> keyMapping = this.createKeyMapping(keys);
        Map<String, CacheValue<V>> cacheValues = this.doGetAll(keyMapping.keySet());
        return this.saveToResult(keyMapping, cacheValues, cacheValues.size());
    }

    @Override
    public CompletableFuture<Map<K, V>> getAllAsync(Set<? extends K> keys) {
        if (keys == null) {
            return this.requireNonNull("keys must not be null.");
        }
        if (keys.isEmpty()) {
            return CompletableFuture.completedFuture(HashMap.newHashMap(0));
        }
        return CompletableFuture.completedFuture(keys)
                .thenApply(this::createKeyMapping)
                .thenCompose(keyMapping -> {
                    Set<String> keySet = keyMapping.keySet();
                    return this.doGetAllAsync(keySet)
                            .thenApply(cacheValues -> this.saveToResult(keyMapping, cacheValues, cacheValues.size()));
                });
    }

    @Override
    public Map<K, V> getAllOrLoad(Set<? extends K> keys) {
        if (this.cacheLoader == null) {
            return this.getAll(keys);
        }
        return this.getAllOrLoad(keys, this.cacheLoader);
    }

    @Override
    public CompletableFuture<Map<K, V>> getAllOrLoadAsync(Set<? extends K> keys) {
        if (this.cacheLoader == null) {
            return this.getAllAsync(keys);
        }
        return this.getAllOrLoadAsync(keys, this.cacheLoader);
    }

    @Override
    public Map<K, V> getAllOrLoad(Set<? extends K> keys, CacheLoader<K, V> cacheLoader) {
        requireNonNull(keys, "keys must not be null.");
        requireNonNull(cacheLoader, "cacheLoader must not be null.");
        if (keys.isEmpty()) {
            return HashMap.newHashMap(0);
        }
        Map<String, K> keyMapping = this.createKeyMapping(keys);
        Map<String, CacheValue<V>> cacheValues = this.doGetAll(keyMapping.keySet());
        Map<K, V> result = this.saveToResult(keyMapping, cacheValues, keyMapping.size());
        return this.loadAndSaveToResult(result, keyMapping, cacheLoader);
    }

    @Override
    public CompletableFuture<Map<K, V>> getAllOrLoadAsync(Set<? extends K> keys, CacheLoader<K, V> cacheLoader) {
        if (keys == null) {
            return this.requireNonNull("keys must not be null.");
        }
        if (cacheLoader == null) {
            return this.requireNonNull("cacheLoader must not be null.");
        }
        if (keys.isEmpty()) {
            return CompletableFuture.completedFuture(HashMap.newHashMap(0));
        }
        return CompletableFuture.completedFuture(keys)
                .thenApply(this::createKeyMapping)
                .thenCompose(keyMapping -> {
                    Set<String> keySet = keyMapping.keySet();
                    return this.doGetAllAsync(keySet)
                            .thenApply(cacheValues -> this.saveToResult(keyMapping, cacheValues, keyMapping.size()))
                            .thenApply(result -> this.loadAndSaveToResult(result, keyMapping, cacheLoader));
                });
    }

    @Override
    public void put(K key, V value) {
        requireNonNull(key, "key must not be null.");
        String storeKey = this.toStoreKey(key);
        this.doPut(storeKey, value);
        this.cacheRefresh.onPut(storeKey);
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        if (key == null) {
            return this.requireNonNull("key must not be null.");
        }
        String storeKey = this.toStoreKey(key);
        return this.doPutAsync(storeKey, value).whenCompleteAsync((vod, t) -> this.cacheRefresh.onPut(storeKey));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        requireNonNull(keyValues, "keyValues must not be null.");
        if (keyValues.isEmpty()) {
            return;
        }
        Map<String, V> kvs = this.toStoreKeyValues(keyValues);
        this.doPutAll(kvs);
        this.cacheRefresh.onPutAll(kvs.keySet());
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> keyValues) {
        if (keyValues == null) {
            return this.requireNonNull("keyValues must not be null.");
        }
        if (keyValues.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(keyValues)
                .thenApply(this::toStoreKeyValues)
                .whenCompleteAsync((kvs, t) -> this.cacheRefresh.onPutAll(kvs.keySet()))
                .thenCompose(this::doPutAllAsync);
    }


    @Override
    public void remove(K key) {
        requireNonNull(key, "key must not be null.");
        String storeKey = this.toStoreKey(key);

        this.doRemove(storeKey);
        this.cacheRefresh.onRemove(storeKey);
    }

    @Override
    public CompletableFuture<Void> removeAsync(K key) {
        if (key == null) {
            return this.requireNonNull("key must not be null.");
        }
        String storeKey = this.toStoreKey(key);
        return this.doRemoveAsync(storeKey).whenCompleteAsync((vod, t) -> this.cacheRefresh.onRemove(storeKey));
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        requireNonNull(keys, "keys must not be null.");
        if (keys.isEmpty()) {
            return;
        }
        Set<String> storeKeys = toStoreKeys(keys);
        this.doRemoveAll(storeKeys);
        this.cacheRefresh.onRemoveAll(storeKeys);
    }

    @Override
    public CompletableFuture<Void> removeAllAsync(Set<? extends K> keys) {
        if (keys == null) {
            return this.requireNonNull("keys must not be null.");
        }
        if (keys.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        Set<String> storeKeys = toStoreKeys(keys);
        return this.doRemoveAllAsync(storeKeys).whenCompleteAsync((vod, t) -> this.cacheRefresh.onRemoveAll(storeKeys));
    }

    private Map<String, V> toStoreKeyValues(Map<? extends K, ? extends V> keyValues) {
        Map<String, V> kvs = HashMap.newHashMap(keyValues.size());
        keyValues.forEach((key, value) -> {
            requireNonNull(key, "keyValues has null element.");
            kvs.put(toStoreKey(key), value);
        });
        return kvs;
    }

    private Set<String> toStoreKeys(Set<? extends K> keys) {
        Set<String> storeKeys = HashSet.newHashSet(keys.size());
        for (K key : keys) {
            requireNonNull(key, "keys has null element.");
            storeKeys.add(toStoreKey(key));
        }
        return storeKeys;
    }

    private String toStoreKey(K key) {
        return this.keyCodec.encode(key);
    }

    private K fromStoreKey(String storeKey) {
        return this.keyCodec.decode(storeKey);
    }

    private void requireNonNull(Object obj, String tips) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(this.message, tips));
        }
    }

    private <T> CompletableFuture<T> requireNonNull(String tips) {
        String msg = String.format(this.message, tips);
        return CompletableFuture.failedFuture(new IllegalArgumentException(msg));
    }

    /**
     * 1. 创建 “原生键” 与 “缓存键” 的映射；<p>
     * 2. 保存访问记录，用于缓存数据刷新。
     *
     * @param keys 输入的键集合
     * @return 返回一个 Map对象，其键为缓存键，值为原始键
     */
    private Map<String, K> createKeyMapping(Set<? extends K> keys) {
        Map<String, K> keyMapping = HashMap.newHashMap(keys.size());
        for (K key : keys) {
            requireNonNull(key, "keys has null element.");
            keyMapping.put(toStoreKey(key), key);
        }
        return keyMapping;
    }

    private Map<K, V> loadAndSaveToResult(Map<K, V> result, Map<String, K> keyMapping, CacheLoader<K, V> cacheLoader) {
        // 1. 如果缓存已命中全部数据，直接返回缓存结果集
        if (keyMapping.isEmpty()) {
            return result;
        }
        // 2. 回源取值
        int hitLoads = 0;
        Map<K, V> loaded = this.loadAll(keyMapping, cacheLoader);
        // 3. 用于将回源取值结果存入缓存
        Map<String, V> toCache = HashMap.newHashMap(keyMapping.size());
        // 4. 回源取值结果存入最终结果集
        for (Map.Entry<String, K> entry : keyMapping.entrySet()) {
            String storeKey = entry.getKey();
            K key = entry.getValue();
            V value = loaded.get(key);
            toCache.put(storeKey, value);
            if (value != null) {
                result.put(key, value);
                ++hitLoads;
            }
        }
        // 5. 缓存回源取值结果
        this.doPutAndRefresh(toCache, hitLoads);
        // 6. 返回最终结果集
        return result;
    }

    private Map<K, V> saveToResult(Map<String, K> keyMapping, Map<String, CacheValue<V>> cacheValues, int size) {
        Map<K, V> result = HashMap.newHashMap(size);
        for (Map.Entry<String, CacheValue<V>> entry : cacheValues.entrySet()) {
            String storeKey = entry.getKey();
            CacheValue<V> cacheValue = entry.getValue();
            if (cacheValue != null) {
                K key = keyMapping.remove(storeKey);
                if (cacheValue.hasValue()) {
                    result.put(key, cacheValue.getValue());
                }
            }
        }
        return result;
    }

    private Map<K, CacheValue<V>> saveToWrapperResult(Map<String, K> keyMapping, Map<String, CacheValue<V>> cacheValues) {
        Map<K, CacheValue<V>> result = HashMap.newHashMap(cacheValues.size());
        for (Map.Entry<String, CacheValue<V>> entry : cacheValues.entrySet()) {
            String storeKey = entry.getKey();
            CacheValue<V> cacheValue = entry.getValue();
            if (cacheValue != null) {
                K key = keyMapping.remove(storeKey);
                result.put(key, cacheValue);
            }
        }
        return result;
    }

    private Map<K, V> loadAll(Map<String, K> keyMapping, CacheLoader<K, V> cacheLoader) {
        Set<K> keys = Sets.newHashSet(keyMapping.size());
        keyMapping.forEach((storeKey, key) -> {
            if (containsPredicate.test(key)) {
                keys.add(key);
            }
        });
        if (keys.isEmpty()) {
            return HashMap.newHashMap(0);
        }
        return cacheLoader.loadAll(keys);
    }

    /**
     * 1.数据存入缓存 <br>
     * 2.执行刷新逻辑<br>
     * 3.记录统计信息
     *
     * @param keyValues 回源取值结果集
     * @param hitLoads  回源命中数量
     */
    private void doPutAndRefresh(Map<String, V> keyValues, int hitLoads) {
        int totalLoads = keyValues.size();
        // 1. 回源取值结果存入缓存
        this.doPutAll(keyValues);
        // 2. 执行刷新逻辑
        this.cacheRefresh.onPutAll(keyValues.keySet());
        // 3. 记录回源成功/失败次数
        this.metricsMonitor.incHitLoads(hitLoads);
        this.metricsMonitor.incMissLoads(totalLoads - hitLoads);
    }

    /**
     * 1.数据存入缓存 <br>
     * 2.执行刷新逻辑<br>
     * 3.记录统计信息
     *
     * @param storeKey 缓存键
     * @param value    缓存值
     */
    private void doPutAndRefresh(String storeKey, V value) {
        this.doPut(storeKey, value);
        this.cacheRefresh.onPut(storeKey);
        if (value != null) {
            this.metricsMonitor.incHitLoads(1);
        } else {
            this.metricsMonitor.incMissLoads(1);
        }
    }

    protected abstract CacheValue<V> doGet(String key);

    protected abstract CompletableFuture<CacheValue<V>> doGetAsync(String storeKey);

    protected abstract Map<String, CacheValue<V>> doGetAll(Set<String> keys);

    protected abstract CompletableFuture<Map<String, CacheValue<V>>> doGetAllAsync(Set<String> keys);

    protected abstract void doPut(String key, V value);

    protected abstract CompletableFuture<Void> doPutAsync(String key, V value);

    protected abstract void doPutAll(Map<String, ? extends V> keyValues);

    protected abstract CompletableFuture<Void> doPutAllAsync(Map<String, ? extends V> keyValues);

    protected abstract void doRemove(String key);

    protected abstract CompletableFuture<Void> doRemoveAsync(String key);

    protected abstract void doRemoveAll(Set<String> keys);

    protected abstract CompletableFuture<Void> doRemoveAllAsync(Set<String> keys);

}