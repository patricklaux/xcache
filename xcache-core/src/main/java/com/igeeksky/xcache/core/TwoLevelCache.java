package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.store.StoreProxy;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.props.StoreLevel;
import com.igeeksky.xtool.core.collection.Maps;

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
        this.first = getStore(stores, index, extend.getStatMonitor());
        this.second = getStore(stores, index, extend.getStatMonitor());
    }

    private static <V> Store<V> getStore(Store<V>[] stores, AtomicInteger index, CacheStatMonitor statMonitor) {
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
    protected CompletableFuture<CacheValue<V>> doAsyncGet(String storeKey) {
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
        Map<String, CacheValue<V>> result = addToResult(first.getAllCacheValues(cloneKeys), cloneKeys, keys.size());
        // 如果键集已经为空，直接返回最终结果集（一级缓存已查询到所有数据）
        if (cloneKeys.isEmpty()) {
            return result;
        }
        // 从二级缓存查询数据，并添加到最终结果集
        return addToResult(result, second.getAllCacheValues(cloneKeys), first);
    }

    @Override
    protected CompletableFuture<Map<String, CacheValue<V>>> doAsyncGetAll(Set<String> keys) {
        Set<String> cloneKeys = new HashSet<>(keys);
        return first.getAllCacheValuesAsync(cloneKeys)
                .thenCompose(firstAll -> {
                    Map<String, CacheValue<V>> result = addToResult(firstAll, cloneKeys, keys.size());
                    if (cloneKeys.isEmpty()) {
                        return CompletableFuture.completedFuture(result);
                    }
                    return second.getAllCacheValuesAsync(cloneKeys)
                            .thenApply((secondAll) -> addToResult(result, secondAll, first));
                });
    }

    private static <V> Map<String, CacheValue<V>> addToResult(Map<String, CacheValue<V>> firstAll,
                                                              Set<String> cloneKeys, int size) {
        Map<String, CacheValue<V>> result = Maps.newHashMap(size);
        if (Maps.isNotEmpty(firstAll)) {
            for (Map.Entry<String, CacheValue<V>> entry : firstAll.entrySet()) {
                String key = entry.getKey();
                CacheValue<V> cacheValue = entry.getValue();
                if (cacheValue != null) {
                    result.put(key, cacheValue);
                    cloneKeys.remove(key);
                }
            }
        }
        return result;
    }

    private static <V> Map<String, CacheValue<V>> addToResult(Map<String, CacheValue<V>> result,
                                                              Map<String, CacheValue<V>> secondAll,
                                                              Store<V> first) {
        if (Maps.isNotEmpty(secondAll)) {
            Map<String, V> saveToLower = Maps.newHashMap(secondAll.size());
            for (Map.Entry<String, CacheValue<V>> entry : secondAll.entrySet()) {
                String key = entry.getKey();
                CacheValue<V> cacheValue = entry.getValue();
                if (cacheValue != null) {
                    result.put(key, cacheValue);
                    saveToLower.put(key, cacheValue.getValue());
                }
            }
            // 二级缓存数据保存到一级缓存
            if (Maps.isNotEmpty(saveToLower)) {
                first.putAllAsync(saveToLower);
            }
        }
        return result;
    }

    @Override
    protected void doPut(String key, V value) {
        second.put(key, value);
        first.put(key, value);
        syncMonitor.afterPut(key);
    }

    @Override
    protected CompletableFuture<Void> doAsyncPut(String key, V value) {
        return second.putAsync(key, value)
                .thenCompose(vod -> first.putAsync(key, value))
                .whenCompleteAsync((vod, throwable) -> {
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
    protected CompletableFuture<Void> doAsyncPutAll(Map<String, ? extends V> keyValues) {
        return second.putAllAsync(keyValues)
                .whenCompleteAsync((vod, throwable) -> {
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
    protected CompletableFuture<Void> doAsyncRemove(String key) {
        return second.removeAsync(key)
                .whenCompleteAsync((vod, throwable) -> {
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
    protected CompletableFuture<Void> doAsyncRemoveAll(Set<String> keys) {
        return second.removeAllAsync(keys)
                .whenCompleteAsync((vod, throwable) -> {
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