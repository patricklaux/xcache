package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xcache.props.StoreLevel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 缓存代理类
 * <p>
 * 用于处理缓存的读写操作，并记录缓存命中率等指标
 *
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/12
 */
public class StoreProxy<V> implements Store<V> {

    private final Store<V> store;

    private final StoreLevel level;

    private final CacheStatMonitor statMonitor;

    public StoreProxy(Store<V> store, StoreLevel level, CacheStatMonitor statMonitor) {
        this.store = store;
        this.level = level;
        this.statMonitor = statMonitor;
        this.statMonitor.setCounter(level);
    }

    @Override
    public CacheValue<V> getCacheValue(String key) {
        CacheValue<V> cacheValue = store.getCacheValue(key);
        if (cacheValue != null) {
            statMonitor.incHits(level, 1L);
        } else {
            statMonitor.incMisses(level, 1L);
        }
        return cacheValue;
    }

    @Override
    public CompletableFuture<CacheValue<V>> getCacheValueAsync(String key) {
        return store.getCacheValueAsync(key)
                .whenCompleteAsync((cacheValue, throwable) -> {
                    if (throwable == null) {
                        if (cacheValue != null) {
                            statMonitor.incHits(level, 1L);
                        } else {
                            statMonitor.incMisses(level, 1L);
                        }
                    }
                });
    }

    @Override
    public Map<String, CacheValue<V>> getAllCacheValues(Set<? extends String> keys) {
        int total = keys.size();
        Map<String, CacheValue<V>> result = store.getAllCacheValues(keys);
        int hits = result.size();
        statMonitor.incHits(level, hits);
        statMonitor.incMisses(level, total - hits);
        return result;
    }

    @Override
    public CompletableFuture<Map<String, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends String> keys) {
        int total = keys.size();
        return store.getAllCacheValuesAsync(keys)
                .whenCompleteAsync((result, throwable) -> {
                    if (throwable == null) {
                        int hits = result.size();
                        statMonitor.incHits(level, hits);
                        statMonitor.incMisses(level, total - hits);
                    }
                });
    }

    @Override
    public void put(String key, V value) {
        store.put(key, value);
        statMonitor.incPuts(level, 1L);
    }

    @Override
    public CompletableFuture<Void> putAsync(String key, V value) {
        return this.store.putAsync(key, value)
                .whenCompleteAsync((vod, throwable) -> {
                    if (throwable == null) {
                        statMonitor.incPuts(level, 1L);
                    }
                });
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        int size = keyValues.size();
        store.putAll(keyValues);
        statMonitor.incPuts(level, size);
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends String, ? extends V> keyValues) {
        int size = keyValues.size();
        return store.putAllAsync(keyValues)
                .whenCompleteAsync((vod, throwable) -> {
                    if (throwable == null) {
                        statMonitor.incPuts(level, size);
                    }
                });
    }

    @Override
    public void remove(String key) {
        store.remove(key);
        statMonitor.incRemovals(level, 1L);
    }

    @Override
    public CompletableFuture<Void> removeAsync(String key) {
        return store.removeAsync(key)
                .whenCompleteAsync((vod, throwable) -> {
                    if (throwable == null) {
                        statMonitor.incRemovals(level, 1L);
                    }
                });
    }

    @Override
    public void removeAll(Set<? extends String> keys) {
        int size = keys.size();
        store.removeAll(keys);
        statMonitor.incRemovals(level, size);
    }

    @Override
    public CompletableFuture<Void> removeAllAsync(Set<? extends String> keys) {
        int size = keys.size();
        return store.removeAllAsync(keys)
                .whenCompleteAsync((vod, throwable) -> {
                    if (throwable == null) {
                        statMonitor.incRemovals(level, size);
                    }
                });
    }

    @Override
    public void clear() {
        store.clear();
        statMonitor.incClears(level);
    }

}
