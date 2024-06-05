package com.igeeksky.xcache.core;

import com.igeeksky.xcache.AbstractCache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.StoreType;
import com.igeeksky.xcache.config.CacheConfig;
import com.igeeksky.xcache.extension.loader.CacheLoader;
import com.igeeksky.xcache.extension.statistic.CacheStatMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.store.LocalStore;
import com.igeeksky.xcache.store.RemoteStore;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-23
 */
public class OneLevelCache<K, V> extends AbstractCache<K, V> {

    private final StoreType storeType;

    private final LocalStore localStore;

    private final RemoteStore remoteStore;

    private final CacheStatMonitor statMonitor;

    private final CacheSyncMonitor syncMonitor;

    public OneLevelCache(CacheConfig<K, V> config, LocalStore localStore, RemoteStore remoteStore) {
        super(config);
        this.localStore = localStore;
        this.remoteStore = remoteStore;
        this.storeType = (localStore != null) ? StoreType.LOCAL : StoreType.REMOTE;
        this.statMonitor = config.getStatMonitor();
        this.syncMonitor = config.getSyncMonitor();
    }

    @Override
    protected CacheValue<V> doGet(String key) {
        CacheValue<V> value;
        if (localStore != null) {
            value = this.fromLocalStoreValue(localStore.get(key));
        } else {
            value = this.fromRemoteStoreValue(remoteStore.get(key));
        }

        if (value != null) {
            statMonitor.incHits(storeType, 1L);
        } else {
            statMonitor.incMisses(storeType, 1L);
        }

        return value;
    }

    @Override
    protected V doLoad(K key, String storeKey, CacheLoader<K, V> cacheLoader) {
        V value = cacheLoader.load(key);
        this.doPut(storeKey, value);

        statMonitor.incPuts(storeType, 1L);
        return value;
    }

    @Override
    public Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        int total = keys.size();

        Map<String, CacheValue<V>> result;
        if (localStore != null) {
            Map<String, CacheValue<Object>> localGetAll = localStore.getAll(keys);
            result = Maps.newHashMap(localGetAll.size());
            localGetAll.forEach((key, value) -> result.put(key, fromLocalStoreValue(value)));
        } else {
            Map<String, CacheValue<byte[]>> remoteGetAll = remoteStore.getAll(keys);
            result = Maps.newHashMap(remoteGetAll.size());
            remoteGetAll.forEach((key, value) -> result.put(key, fromRemoteStoreValue(value)));
        }

        int hits = result.size();
        statMonitor.incHits(storeType, hits);
        statMonitor.incMisses(storeType, total - hits);
        return result;
    }

    @Override
    protected void doPut(String key, V value) {
        if (localStore != null) {
            localStore.put(key, toLocalStoreValue(value));
        } else {
            remoteStore.put(key, toRemoteStoreValue(value));
        }
        statMonitor.incPuts(storeType, 1L);
    }

    @Override
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        int total = keyValues.size();

        if (localStore != null) {
            Map<String, Object> kvs = Maps.newHashMap(total);
            keyValues.forEach((k, v) -> kvs.put(k, toLocalStoreValue(v)));
            localStore.putAll(kvs);
        } else {
            Map<String, byte[]> kvs = Maps.newHashMap(total);
            keyValues.forEach((k, v) -> kvs.put(k, toRemoteStoreValue(v)));
            remoteStore.putAll(kvs);
        }

        statMonitor.incPuts(storeType, total);
    }

    @Override
    protected void doEvict(String key) {
        if (localStore != null) {
            localStore.evict(key);
            syncMonitor.afterEvict(key);
        } else {
            remoteStore.evict(key);
        }
        statMonitor.incRemovals(storeType, 1L);
    }

    @Override
    protected void doEvictAll(Set<String> keys) {
        int total = keys.size();

        if (localStore != null) {
            localStore.evictAll(keys);
            syncMonitor.afterEvictAll(keys);
        } else {
            remoteStore.evictAll(keys);
        }

        statMonitor.incRemovals(storeType, total);
    }

    @Override
    public void clear() {
        if (localStore != null) {
            localStore.clear();
            syncMonitor.afterClear();
        } else {
            remoteStore.clear();
        }
        statMonitor.incClears(storeType);
    }

}