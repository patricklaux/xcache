package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.store.StoreProxy;
import com.igeeksky.xcache.extension.metrics.CacheMetricsMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.props.StoreLevel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 两级组合缓存
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public class TwoLevelCache<K, V> extends AbstractCache<K, V> {

    private final Store<V> first;

    private final Store<V> second;

    private final CacheSyncMonitor syncMonitor;

    public TwoLevelCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend, Store<V>[] stores) {
        super(config, extend);
        this.syncMonitor = extend.getSyncMonitor();
        AtomicInteger index = new AtomicInteger(0);
        this.first = getStore(stores, index, extend.getMetricsMonitor());
        this.second = getStore(stores, index, extend.getMetricsMonitor());
    }

    private static <V> Store<V> getStore(Store<V>[] stores, AtomicInteger index, CacheMetricsMonitor statMonitor) {
        StoreLevel[] levels = StoreLevel.values();
        while (index.get() < stores.length) {
            int i = index.getAndIncrement();
            if (stores[i] != null) {
                return new StoreProxy<>(stores[i], levels[i], statMonitor);
            }
        }
        return null;
    }

    @Override
    protected boolean contains(String key) {
        return second.getCacheValue(key) != null;
    }

    @Override
    protected CacheValue<V> doGet(String key) {
        CacheValue<V> cacheValue = first.getCacheValue(key);
        if (cacheValue != null) {
            return cacheValue;
        }

        cacheValue = second.getCacheValue(key);
        if (cacheValue != null) {
            first.put(key, cacheValue.getValue());
        }

        return cacheValue;
    }

    @Override
    protected CompletableFuture<CacheValue<V>> doGetAsync(String storeKey) {
        return first.getCacheValueAsync(storeKey)
                .thenCompose(firstValue -> {
                    if (firstValue != null) {
                        return CompletableFuture.completedFuture(firstValue);
                    }
                    return second.getCacheValueAsync(storeKey)
                            .whenComplete((secondValue, t) -> {
                                if (secondValue != null) {
                                    first.putAsync(storeKey, secondValue.getValue());
                                }
                            });
                });
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        // 复制键集
        Set<String> cloneKeys = new HashSet<>(keys);
        // 从一级缓存查询数据，并添加到最终结果集
        Map<String, CacheValue<V>> firstAll = first.getAllCacheValues(cloneKeys);
        CacheHelper.removeHitKeys(cloneKeys, firstAll);
        // 如果键集已经为空，直接返回最终结果集（一级缓存已查询到所有数据）
        if (cloneKeys.isEmpty()) {
            return firstAll;
        }
        // 从二级缓存查询数据，并添加到最终结果集
        Map<String, CacheValue<V>> secondAll = second.getAllCacheValues(cloneKeys);
        return CacheHelper.mergeResult(firstAll, secondAll, first);
    }

    @Override
    protected CompletableFuture<Map<String, CacheValue<V>>> doGetAllAsync(Set<String> keys) {
        Set<String> cloneKeys = new HashSet<>(keys);
        return first.getAllCacheValuesAsync(cloneKeys)
                .thenCompose(firstAll -> {
                    CacheHelper.removeHitKeys(cloneKeys, firstAll);
                    if (cloneKeys.isEmpty()) {
                        return CompletableFuture.completedFuture(firstAll);
                    }
                    return second.getAllCacheValuesAsync(cloneKeys)
                            .thenApply((secondAll) -> CacheHelper.mergeResult(firstAll, secondAll, first));
                });
    }

    @Override
    protected void doPut(String key, V value) {
        second.put(key, value);
        first.put(key, value);
        syncMonitor.afterPut(key);
    }

    @Override
    protected CompletableFuture<Void> doPutAsync(String key, V value) {
        return second.putAsync(key, value)
                .thenCompose(vod -> first.putAsync(key, value))
                .whenComplete((vod, throwable) -> {
                    if (throwable == null) {
                        syncMonitor.afterPut(key);
                    }
                });
    }

    @Override
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        second.putAll(keyValues);
        first.putAll(keyValues);
        syncMonitor.afterPutAll(keyValues.keySet());
    }

    @Override
    protected CompletableFuture<Void> doPutAllAsync(Map<String, ? extends V> keyValues) {
        return second.putAllAsync(keyValues)
                .whenComplete((vod, throwable) -> {
                    if (throwable == null) {
                        syncMonitor.afterPutAll(keyValues.keySet());
                    }
                })
                .thenCompose(vod -> first.putAllAsync(keyValues));
    }

    @Override
    protected void doRemove(String key) {
        second.remove(key);
        first.remove(key);
        syncMonitor.afterRemove(key);
    }

    @Override
    protected CompletableFuture<Void> doRemoveAsync(String key) {
        return second.removeAsync(key)
                .whenComplete((vod, throwable) -> {
                    if (throwable == null) {
                        syncMonitor.afterRemove(key);
                    }
                })
                .thenCompose(vod -> first.removeAsync(key));
    }

    @Override
    protected void doRemoveAll(Set<String> keys) {
        second.removeAll(keys);
        first.removeAll(keys);
        syncMonitor.afterRemoveAll(keys);
    }

    @Override
    protected CompletableFuture<Void> doRemoveAllAsync(Set<String> keys) {
        return second.removeAllAsync(keys)
                .whenComplete((vod, throwable) -> {
                    if (throwable == null) {
                        syncMonitor.afterRemoveAll(keys);
                    }
                })
                .thenCompose(vod -> first.removeAllAsync(keys));
    }

    @Override
    public void clear() {
        second.clear();
        first.clear();
        syncMonitor.afterClear();
    }

}