package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.store.StoreProxy;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.props.StoreLevel;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-23
 */
public class OneLevelCache<K, V> extends AbstractCache<K, V> {

    private final Store<V> store;

    private final CacheSyncMonitor syncMonitor;

    public OneLevelCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend, Store<V>[] stores) {
        super(config, extend);
        this.syncMonitor = extend.getSyncMonitor();
        store = getStore(extend.getStatMonitor(), stores);
    }

    private Store<V> getStore(CacheStatMonitor statMonitor, Store<V>[] stores) {
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
        return store.get(key);
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        return store.getAll(keys);
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
    protected void doEvict(String key) {
        store.evict(key);
    }

    @Override
    protected void doEvictAll(Set<String> keys) {
        store.evictAll(keys);
    }

    @Override
    public void clear() {
        store.clear();
        syncMonitor.afterClear();
    }

}