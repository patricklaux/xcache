package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.store.StoreProxy;
import com.igeeksky.xcache.extension.metrics.CacheMetricsMonitor;
import com.igeeksky.xcache.props.StoreLevel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 仅有一级缓存时，使用此实现类
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-23
 */
public class OneLevelCache<K, V> extends AbstractCache<K, V> {

    private final Store<V> store;

    public OneLevelCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend, Store<V>[] stores) {
        super(config, extend);
        this.store = getStore(stores, extend.getMetricsMonitor());
    }

    private static <V> Store<V> getStore(Store<V>[] stores, CacheMetricsMonitor statMonitor) {
        StoreLevel[] levels = StoreLevel.values();
        for (int i = 0; i < stores.length; i++) {
            if (stores[i] != null) {
                return new StoreProxy<>(stores[i], levels[i], statMonitor);
            }
        }
        return null;
    }

    @Override
    protected boolean contains(String key) {
        return store.getCacheValue(key) != null;
    }

    @Override
    protected CacheValue<V> doGet(String key) {
        return store.getCacheValue(key);
    }

    @Override
    protected CompletableFuture<CacheValue<V>> doAsyncGet(String storeKey) {
        return store.getCacheValueAsync(storeKey);
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        return store.getAllCacheValues(keys);
    }

    @Override
    protected CompletableFuture<Map<String, CacheValue<V>>> doAsyncGetAll(Set<String> keys) {
        return store.getAllCacheValuesAsync(keys);
    }

    @Override
    protected void doPut(String key, V value) {
        store.put(key, value);
    }

    @Override
    protected CompletableFuture<Void> doAsyncPut(String key, V value) {
        return store.putAsync(key, value);
    }

    @Override
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        store.putAll(keyValues);
    }

    @Override
    protected CompletableFuture<Void> doAsyncPutAll(Map<String, ? extends V> keyValues) {
        return store.putAllAsync(keyValues);
    }

    @Override
    protected void doRemove(String key) {
        store.remove(key);
    }

    @Override
    protected CompletableFuture<Void> doAsyncRemove(String key) {
        return store.removeAsync(key);
    }

    @Override
    protected void doRemoveAll(Set<String> keys) {
        store.removeAll(keys);
    }

    @Override
    protected CompletableFuture<Void> doAsyncRemoveAll(Set<String> keys) {
        return store.removeAllAsync(keys);
    }

    @Override
    public void clear() {
        store.clear();
    }

}