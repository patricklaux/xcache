package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.store.StoreProxy;
import com.igeeksky.xcache.extension.metrics.CacheMetricsMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.props.StoreLevel;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 三级组合缓存
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public class ThreeLevelCache<K, V> extends AbstractCache<K, V> {

    private static final int LENGTH = 3;
    private final CacheSyncMonitor syncMonitor;
    @SuppressWarnings("unchecked")
    private final Store<V>[] stores = new Store[LENGTH];

    public ThreeLevelCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend, Store<V>[] stores) {
        super(config, extend);
        this.syncMonitor = extend.getSyncMonitor();
        CacheMetricsMonitor statMonitor = extend.getMetricsMonitor();
        StoreLevel[] levels = StoreLevel.values();
        for (int i = 0; i < LENGTH; i++) {
            this.stores[i] = new StoreProxy<>(stores[i], levels[i], statMonitor);
        }
    }

    @Override
    protected boolean contains(String key) {
        return stores[2].getCacheValue(key) != null;
    }

    @Override
    protected CacheValue<V> doGet(String key) {
        CacheValue<V> cacheValue = stores[0].getCacheValue(key);
        if (cacheValue != null) {
            return cacheValue;
        }
        cacheValue = stores[1].getCacheValue(key);
        if (cacheValue != null) {
            stores[0].put(key, cacheValue.getValue());
            return cacheValue;
        }
        cacheValue = stores[2].getCacheValue(key);
        if (cacheValue != null) {
            stores[1].put(key, cacheValue.getValue());
            stores[0].put(key, cacheValue.getValue());
        }
        return cacheValue;
    }

    @Override
    protected CompletableFuture<CacheValue<V>> doAsyncGet(String storeKey) {
        return stores[0].getCacheValueAsync(storeKey)
                .thenCompose(firstValue -> {
                    if (firstValue != null) {
                        return CompletableFuture.completedFuture(firstValue);
                    }
                    return stores[1].getCacheValueAsync(storeKey)
                            .thenCompose(secondValue -> {
                                if (secondValue != null) {
                                    stores[0].putAsync(storeKey, secondValue.getValue());
                                    return CompletableFuture.completedFuture(secondValue);
                                }
                                return stores[2].getCacheValueAsync(storeKey)
                                        .whenComplete((thirdValue, t) -> {
                                            if (thirdValue != null) {
                                                V value = thirdValue.getValue();
                                                stores[1].putAsync(storeKey, value)
                                                        .thenCompose(vod -> stores[0].putAsync(storeKey, value));
                                            }
                                        });
                            });
                });
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        Set<String> cloneKeys = new HashSet<>(keys);
        Map<String, CacheValue<V>> result = addToResult(stores[0].getAllCacheValues(cloneKeys), cloneKeys, keys.size());
        if (cloneKeys.isEmpty()) {
            return result;
        }
        addToResult(result, cloneKeys, stores[1].getAllCacheValues(cloneKeys), stores[0]);
        if (cloneKeys.isEmpty()) {
            return result;
        }
        addToResult(result, cloneKeys, stores[2].getAllCacheValues(cloneKeys), stores[0], stores[1]);
        return result;
    }

    @Override
    protected CompletableFuture<Map<String, CacheValue<V>>> doAsyncGetAll(Set<String> keys) {
        Set<String> cloneKeys = new HashSet<>(keys);
        return stores[0].getAllCacheValuesAsync(cloneKeys)
                .thenCompose(firstAll -> {
                    Map<String, CacheValue<V>> result = addToResult(firstAll, cloneKeys, keys.size());
                    if (cloneKeys.isEmpty()) {
                        return CompletableFuture.completedFuture(result);
                    }
                    return stores[1].getAllCacheValuesAsync(cloneKeys)
                            .thenCompose(secondAll -> {
                                addToResult(result, cloneKeys, secondAll, stores[0]);
                                if (cloneKeys.isEmpty()) {
                                    return CompletableFuture.completedFuture(result);
                                }
                                return stores[2].getAllCacheValuesAsync(cloneKeys)
                                        .thenApply(thirdAll -> {
                                            addToResult(result, cloneKeys, thirdAll, stores[0], stores[1]);
                                            return result;
                                        });
                            });
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

    @SafeVarargs
    private static <V> void addToResult(Map<String, CacheValue<V>> result, Set<String> cloneKeys,
                                        Map<String, CacheValue<V>> cacheValues, Store<V>... lowerStores) {
        if (Maps.isNotEmpty(cacheValues)) {
            Map<String, V> saveToLower = Maps.newHashMap(cacheValues.size());
            for (Map.Entry<String, CacheValue<V>> entry : cacheValues.entrySet()) {
                String key = entry.getKey();
                CacheValue<V> cacheValue = entry.getValue();
                if (cacheValue != null) {
                    result.put(key, cacheValue);
                    cloneKeys.remove(key);
                    saveToLower.put(key, cacheValue.getValue());
                }
            }
            // 高层缓存数据保存到低层缓存
            if (Maps.isNotEmpty(saveToLower)) {
                CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
                for (int i = lowerStores.length - 1; i >= 0; i--) {
                    Store<V> store = lowerStores[i];
                    future = future.thenCompose(ignored -> store.putAllAsync(saveToLower));
                }
            }
        }
    }

    @Override
    protected void doPut(String key, V value) {
        stores[2].put(key, value);
        stores[1].put(key, value);
        stores[0].put(key, value);
        syncMonitor.afterPut(key);
    }

    @Override
    protected CompletableFuture<Void> doAsyncPut(String key, V value) {
        return stores[2].putAsync(key, value)
                .thenCompose(ignored -> stores[1].putAsync(key, value))
                .thenCompose(ignored -> stores[0].putAsync(key, value))
                .whenCompleteAsync((ignored, throwable) -> {
                    if (throwable == null) {
                        syncMonitor.afterPut(key);
                    }
                });
    }

    @Override
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        stores[2].putAll(keyValues);
        stores[1].putAll(keyValues);
        stores[0].putAll(keyValues);
        syncMonitor.afterPutAll(keyValues.keySet());
    }

    @Override
    protected CompletableFuture<Void> doAsyncPutAll(Map<String, ? extends V> keyValues) {
        return stores[2].putAllAsync(keyValues)
                .thenCompose(ignored -> stores[1].putAllAsync(keyValues))
                .thenCompose(ignored -> stores[0].putAllAsync(keyValues))
                .whenCompleteAsync((ignored, throwable) -> {
                    if (throwable == null) {
                        syncMonitor.afterPutAll(keyValues.keySet());
                    }
                });
    }

    @Override
    protected void doRemove(String key) {
        stores[2].remove(key);
        stores[1].remove(key);
        stores[0].remove(key);
        syncMonitor.afterRemove(key);
    }

    @Override
    protected CompletableFuture<Void> doAsyncRemove(String key) {
        return stores[2].removeAsync(key)
                .thenCompose(ignored -> stores[1].removeAsync(key))
                .thenCompose(ignored -> stores[0].removeAsync(key))
                .whenCompleteAsync((ignored, throwable) -> {
                    if (throwable == null) {
                        syncMonitor.afterRemove(key);
                    }
                });
    }

    @Override
    protected void doRemoveAll(Set<String> keys) {
        stores[2].removeAll(keys);
        stores[1].removeAll(keys);
        stores[0].removeAll(keys);
        syncMonitor.afterRemoveAll(keys);
    }

    @Override
    protected CompletableFuture<Void> doAsyncRemoveAll(Set<String> keys) {
        return stores[2].removeAllAsync(keys)
                .thenCompose(ignored -> stores[1].removeAllAsync(keys))
                .thenCompose(ignored -> stores[0].removeAllAsync(keys))
                .whenCompleteAsync((ignored, throwable) -> {
                    if (throwable == null) {
                        syncMonitor.afterRemoveAll(keys);
                    }
                });
    }

    @Override
    public void clear() {
        stores[2].clear();
        stores[1].clear();
        stores[0].clear();
        syncMonitor.afterClear();
    }

}