package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.ContainsPredicate;
import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xcache.extension.metrics.CacheMetricsMonitor;
import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProxy;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

import java.util.*;
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

    // 错误信息公共模板
    private final String error;

    public AbstractCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend) {
        this.name = config.getName();
        this.keyType = config.getKeyType();
        this.valueType = config.getValueType();
        this.error = "Cache:[" + this.name + "], %s";

        this.keyCodec = extend.getKeyCodec();
        this.metricsMonitor = extend.getMetricsMonitor();

        this.lockService = extend.getLockService();
        this.containsPredicate = extend.getContainsPredicate();
        this.setCacheRefresh(extend.getCacheRefresh(), extend.getCacheLoader());
    }

    private void setCacheRefresh(CacheRefresh cacheRefresh, CacheLoader<K, V> cacheLoader) {
        if (cacheRefresh != null) {
            requireNonNull(cacheLoader, error, "CacheRefresh depends on the cacheLoader," +
                    " cacheLoader must not be null");
        }
        // 此抽象类的 getOrLoad 和 getOrLoadAll 方法，需要根据 cacheLoader 是否为空来判断是否加锁数据回源
        // 因此当 cacheLoader 为空时，不能赋值 为 NoopCacheLoader
        this.cacheLoader = cacheLoader;
        this.cacheRefresh = new CacheRefreshProxy(name, cacheRefresh);
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
        return fromCacheValue(this.getCacheValue(key));
    }

    @Override
    public CompletableFuture<V> getAsync(K key) {
        return this.getCacheValueAsync(key).thenApply(AbstractCache::fromCacheValue);
    }

    @Override
    public CacheValue<V> getCacheValue(K key) {
        return this.doGet(this.toStoreKey(key));
    }

    @Override
    public CompletableFuture<CacheValue<V>> getCacheValueAsync(K key) {
        return CompletableFuture.completedFuture(key)
                .thenApply(this::toStoreKey)
                .thenCompose(this::doGetAsync);
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
        requireNonNull(cacheLoader, error, "cacheLoader must not be null");
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
        if (cacheLoader == null) {
            String errorMsg = String.format(error, "cacheLoader must not be null.");
            return CompletableFuture.failedFuture(new IllegalArgumentException(errorMsg));
        }
        return CompletableFuture.completedFuture(key)
                .thenApply(this::toStoreKey)
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
        Map<String, K> keyMapping = this.createKeyMapping(keys);
        if (Maps.isEmpty(keyMapping)) {
            return Collections.emptyMap();
        }
        return toKeyCacheValues(keyMapping, this.doGetAll(Collections.unmodifiableSet(keyMapping.keySet())));
    }

    @Override
    public CompletableFuture<Map<K, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends K> keys) {
        return CompletableFuture.completedFuture(keys)
                .thenApply(this::createKeyMapping)
                .thenCompose(keyMapping -> {
                    if (Maps.isEmpty(keyMapping)) {
                        return CompletableFuture.completedFuture(Collections.emptyMap());
                    }
                    return this.doGetAllAsync(Collections.unmodifiableSet(keyMapping.keySet()))
                            .thenApply(cacheValues -> toKeyCacheValues(keyMapping, cacheValues));
                });
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        Map<String, K> keyMapping = this.createKeyMapping(keys);
        if (Maps.isEmpty(keyMapping)) {
            return Collections.emptyMap();
        }
        Map<String, CacheValue<V>> cacheValues = this.doGetAll(Collections.unmodifiableSet(keyMapping.keySet()));
        return fromKeyCacheValues(keyMapping, cacheValues, cacheValues.size());
    }

    @Override
    public CompletableFuture<Map<K, V>> getAllAsync(Set<? extends K> keys) {
        return CompletableFuture.completedFuture(keys)
                .thenApply(this::createKeyMapping)
                .thenCompose(keyMapping -> {
                    if (Maps.isEmpty(keyMapping)) {
                        return CompletableFuture.completedFuture(Collections.emptyMap());
                    }
                    return this.doGetAllAsync(Collections.unmodifiableSet(keyMapping.keySet()))
                            .thenApply(cacheValues -> fromKeyCacheValues(keyMapping, cacheValues, cacheValues.size()));
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
        requireNonNull(cacheLoader, error, "cacheLoader must not be null.");
        Map<String, K> keyMapping = this.createKeyMapping(keys);
        if (Maps.isEmpty(keyMapping)) {
            return Collections.emptyMap();
        }
        Map<String, CacheValue<V>> cacheValues = this.doGetAll(Collections.unmodifiableSet(keyMapping.keySet()));
        Map<K, V> result = fromKeyCacheValues(keyMapping, cacheValues, keyMapping.size());
        return this.loadAndConvert(result, keyMapping, cacheLoader);
    }

    @Override
    public CompletableFuture<Map<K, V>> getAllOrLoadAsync(Set<? extends K> keys, CacheLoader<K, V> cacheLoader) {
        if (cacheLoader == null) {
            String errorMsg = String.format(error, "cacheLoader must not be null.");
            return CompletableFuture.failedFuture(new IllegalArgumentException(errorMsg));
        }
        return CompletableFuture.completedFuture(keys)
                .thenApply(this::createKeyMapping)
                .thenCompose(keyMapping -> {
                    if (Maps.isEmpty(keyMapping)) {
                        return CompletableFuture.completedFuture(Collections.emptyMap());
                    }
                    return this.doGetAllAsync(Collections.unmodifiableSet(keyMapping.keySet()))
                            .thenApply(cacheValues -> fromKeyCacheValues(keyMapping, cacheValues, keyMapping.size()))
                            .thenApply(result -> this.loadAndConvert(result, keyMapping, cacheLoader));
                });
    }

    @Override
    public void put(K key, V value) {
        String storeKey = this.toStoreKey(key);
        this.cacheRefresh.onPut(storeKey);
        this.doPut(storeKey, value);
    }

    @Override
    public CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.completedFuture(key)
                .thenApply(this::toStoreKey)
                .thenCompose(storeKey -> {
                    this.cacheRefresh.onPut(storeKey);
                    return this.doPutAsync(storeKey, value);
                });
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        Map<String, V> kvs = this.toStoreKeyValues(keyValues);
        if (Maps.isEmpty(kvs)) {
            return;
        }
        this.cacheRefresh.onPutAll(kvs.keySet());
        this.doPutAll(kvs);
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> keyValues) {
        return CompletableFuture.completedFuture(keyValues)
                .thenApply(this::toStoreKeyValues)
                .thenCompose(kvs -> {
                    if (Maps.isEmpty(kvs)) {
                        return CompletableFuture.completedFuture(null);
                    }
                    this.cacheRefresh.onPutAll(kvs.keySet());
                    return this.doPutAllAsync(kvs);
                });
    }

    @Override
    public void remove(K key) {
        String storeKey = this.toStoreKey(key);
        this.cacheRefresh.onRemove(storeKey);
        this.doRemove(storeKey);
    }

    @Override
    public CompletableFuture<Void> removeAsync(K key) {
        return CompletableFuture.completedFuture(key)
                .thenApply(this::toStoreKey)
                .thenCompose(storeKey -> {
                    this.cacheRefresh.onRemove(storeKey);
                    return this.doRemoveAsync(storeKey);
                });
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        Set<String> storeKeys = this.toStoreKeys(keys);
        if (CollectionUtils.isEmpty(storeKeys)) {
            return;
        }
        this.doRemoveAll(storeKeys);
        this.cacheRefresh.onRemoveAll(storeKeys);
    }

    @Override
    public CompletableFuture<Void> removeAllAsync(Set<? extends K> keys) {
        return CompletableFuture.completedFuture(keys)
                .thenApply(this::toStoreKeys)
                .thenCompose(ks -> {
                    if (CollectionUtils.isEmpty(ks)) {
                        return CompletableFuture.completedFuture(null);
                    }
                    this.cacheRefresh.onRemoveAll(ks);
                    return this.doRemoveAllAsync(ks);
                });
    }

    /**
     * 1. 创建 “原生键” 与 “缓存键” 的映射；<p>
     * 2. 保存访问记录，用于缓存数据刷新。
     *
     * @param keys 输入的键集合
     * @return 返回一个 Map对象，其键为缓存键，值为原始键
     */
    private Map<String, K> createKeyMapping(Set<? extends K> keys) {
        requireNonNull(keys, error, "keys must not be null.");
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, K> keyMapping = HashMap.newHashMap(keys.size());
        for (K key : keys) {
            keyMapping.put(this.toStoreKey(key), key);
        }
        return keyMapping;
    }

    private Map<K, V> loadAndConvert(Map<K, V> result, Map<String, K> keyMapping, CacheLoader<K, V> cacheLoader) {
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

    private Map<K, V> loadAll(Map<String, K> keyMapping, CacheLoader<K, V> cacheLoader) {
        Set<K> keys = HashSet.newHashSet(keyMapping.size());
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
        // 1. 执行刷新逻辑
        this.cacheRefresh.onPutAll(keyValues.keySet());
        // 2. 回源取值结果存入缓存
        this.doPutAll(keyValues);
        // 3. 记录回源成功/失败次数
        this.metricsMonitor.incHitLoads(hitLoads);
        this.metricsMonitor.incMissLoads(totalLoads - hitLoads);
    }

    /**
     * 1.执行刷新逻辑 <br>
     * 2.数据存入缓存 <br>
     * 3.记录统计信息
     *
     * @param storeKey 缓存键
     * @param value    缓存值
     */
    private void doPutAndRefresh(String storeKey, V value) {
        this.cacheRefresh.onPut(storeKey);
        this.doPut(storeKey, value);
        if (value != null) {
            this.metricsMonitor.incHitLoads(1);
        } else {
            this.metricsMonitor.incMissLoads(1);
        }
    }

    private Map<String, V> toStoreKeyValues(Map<? extends K, ? extends V> keyValues) {
        requireNonNull(keyValues, error, "keyValues must not be null.");
        if (keyValues.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, V> kvs = HashMap.newHashMap(keyValues.size());
        keyValues.forEach((key, value) -> kvs.put(toStoreKey(key), value));
        return kvs;
    }

    private Set<String> toStoreKeys(Set<? extends K> keys) {
        requireNonNull(keys, error, "keys must not be null.");
        if (keys.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> storeKeys = HashSet.newHashSet(keys.size());
        for (K key : keys) {
            storeKeys.add(this.toStoreKey(key));
        }
        return storeKeys;
    }

    private String toStoreKey(K key) {
        requireNonNull(key, error, "key must not be null.");
        String storeKey = this.keyCodec.encode(key);
        if (storeKey == null) {
            String tips = "key:[" + key + "] encode result must not be null.";
            throw new IllegalArgumentException(String.format(error, tips));
        }
        return storeKey;
    }

    private K fromStoreKey(String storeKey) {
        requireNonNull(storeKey, error, "storeKey must not be null.");
        K key = this.keyCodec.decode(storeKey);
        if (key == null) {
            String tips = "storeKey:[" + storeKey + "] decode result must not be null.";
            throw new IllegalArgumentException(String.format(error, tips));
        }
        return key;
    }

    private static <K, V> Map<K, CacheValue<V>> toKeyCacheValues(Map<String, K> keyMapping,
                                                                 Map<String, CacheValue<V>> cacheValues) {
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

    private static <K, V> Map<K, V> fromKeyCacheValues(Map<String, K> keyMapping,
                                                       Map<String, CacheValue<V>> cacheValues, int size) {
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

    private static <V> V fromCacheValue(CacheValue<V> cacheValue) {
        return (cacheValue != null) ? cacheValue.getValue() : null;
    }

    private static void requireNonNull(Object obj, String format, String tips) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(format, tips));
        }
    }

    /**
     * 最后一级缓存是否存在键对应的值
     *
     * @param storeKey 缓存键
     * @return {@code true} 存在； {@code false} 不存在
     */
    protected abstract boolean contains(String storeKey);

    protected abstract CacheValue<V> doGet(String key);

    protected abstract CompletableFuture<CacheValue<V>> doGetAsync(String storeKey);

    protected abstract Map<String, CacheValue<V>> doGetAll(Set<String> keys);

    /**
     * 异步从缓存获取数据集
     *
     * @param unmodifiableKeys 缓存键集合（不可修改：如果可能修改集合元素，请制作副本后再操作）
     */
    protected abstract CompletableFuture<Map<String, CacheValue<V>>> doGetAllAsync(Set<String> unmodifiableKeys);

    protected abstract void doPut(String key, V value);

    protected abstract CompletableFuture<Void> doPutAsync(String key, V value);

    protected abstract void doPutAll(Map<String, ? extends V> keyValues);

    protected abstract CompletableFuture<Void> doPutAllAsync(Map<String, ? extends V> keyValues);

    protected abstract void doRemove(String key);

    protected abstract CompletableFuture<Void> doRemoveAsync(String key);

    protected abstract void doRemoveAll(Set<String> keys);

    protected abstract CompletableFuture<Void> doRemoveAllAsync(Set<String> keys);

}