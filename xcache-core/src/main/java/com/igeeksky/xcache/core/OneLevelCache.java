package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.store.StoreProxy;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.props.StoreLevel;

import java.util.Map;
import java.util.Set;

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

    private final CacheSyncMonitor syncMonitor;

    public OneLevelCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend, Store<V>[] stores) {
        super(config, extend);
        this.syncMonitor = extend.getSyncMonitor();
        this.store = getStore(stores, extend.getStatMonitor());
    }

    private static <V> Store<V> getStore(Store<V>[] stores, CacheStatMonitor statMonitor) {
        StoreLevel[] levels = StoreLevel.values();
        for (int i = 0; i < stores.length; i++) {
            if (stores[i] != null) {
                return new StoreProxy<>(stores[i], levels[i], statMonitor);
            }
        }
        return null;
    }

    @Override
    protected CacheValue<V> doGet(String key) {
        return store.getCacheValue(key);
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        return store.getAllCacheValues(keys);
    }

    @Override
    protected void doPut(String key, V value) {
        store.put(key, value);
    }

    @Override
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        store.putAll(keyValues);
    }

    @Override
    protected void doRemove(String key) {
        store.remove(key);
    }

    @Override
    protected void doRemoveAll(Set<String> keys) {
        store.removeAll(keys);
    }

    @Override
    public void clear() {
        store.clear();
        syncMonitor.afterClear();
    }

}