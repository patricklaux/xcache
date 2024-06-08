package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.StoreType;
import com.igeeksky.xcache.core.config.CacheConfig;
import com.igeeksky.xcache.core.store.LocalStore;
import com.igeeksky.xcache.core.store.RemoteStore;
import com.igeeksky.xcache.extension.loader.CacheLoader;
import com.igeeksky.xcache.extension.statistic.CacheStatMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.Map;
import java.util.Set;

/**
 * 两级组合缓存
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public class TwoLevelCache<K, V> extends AbstractCache<K, V> {

    private final LocalStore localStore;

    private final RemoteStore remoteStore;

    private final CacheStatMonitor statMonitor;

    private final CacheSyncMonitor syncMonitor;

    public TwoLevelCache(CacheConfig<K, V> config, LocalStore localStore, RemoteStore remoteStore) {
        super(config);
        this.localStore = localStore;
        this.remoteStore = remoteStore;
        this.statMonitor = config.getStatMonitor();
        this.syncMonitor = config.getSyncMonitor();
    }

    @Override
    protected CacheValue<V> doGet(String key) {
        CacheValue<V> cacheValue = this.fromLocalStoreValue(localStore.get(key));
        if (cacheValue != null) {
            statMonitor.incHits(StoreType.LOCAL, 1L);
            return cacheValue;
        }
        statMonitor.incMisses(StoreType.LOCAL, 1L);

        cacheValue = this.fromRemoteStoreValue(remoteStore.get(key));
        if (cacheValue != null) {
            localStore.put(key, this.toLocalStoreValue(cacheValue.getValue()));

            statMonitor.incHits(StoreType.REMOTE, 1L);
            statMonitor.incPuts(StoreType.LOCAL, 1L);
        } else {
            statMonitor.incMisses(StoreType.REMOTE, 1L);
        }

        return cacheValue;
    }

    @Override
    protected V doLoad(K key, String storeKey, CacheLoader<K, V> cacheLoader) {
        V value = cacheLoader.load(key);
        this.doPut(storeKey, value);

        statMonitor.incLoads();
        return value;
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        int total = keys.size();
        Map<String, CacheValue<V>> result = Maps.newHashMap(total);

        // 1. 从本地缓存读取数据
        Map<String, CacheValue<Object>> localGetAll = localStore.getAll(keys);
        keys.removeAll(localGetAll.keySet());

        int localHits = localGetAll.size();
        statMonitor.incHits(StoreType.LOCAL, localHits);
        statMonitor.incMisses(StoreType.LOCAL, total - localHits);

        localGetAll.forEach((key, value) -> result.put(key, this.fromLocalStoreValue(value)));

        int remoteSize = keys.size();
        if (remoteSize == 0) {
            return result;
        }

        // 2. 从远程缓存读取未命中数据
        Map<String, CacheValue<byte[]>> remoteGetAll = remoteStore.getAll(keys);
        if (remoteGetAll.isEmpty()) {
            statMonitor.incMisses(StoreType.REMOTE, remoteSize);
            return result;
        }

        int remoteHits = remoteGetAll.size();
        statMonitor.incHits(StoreType.REMOTE, remoteHits);
        statMonitor.incMisses(StoreType.REMOTE, remoteSize - remoteHits);

        // 3. 远程缓存数据保存到本地缓存
        Map<String, V> saveToLocal = Maps.newHashMap(remoteHits);
        remoteGetAll.forEach((key, value) -> {
            CacheValue<V> cacheValue = this.fromRemoteStoreValue(value);
            result.put(key, cacheValue);
            saveToLocal.put(key, cacheValue.getValue());
        });

        localStore.putAll(saveToLocal);

        statMonitor.incPuts(StoreType.LOCAL, saveToLocal.size());

        return result;
    }

    @Override
    protected void doPut(String key, V value) {
        remoteStore.put(key, toRemoteStoreValue(value));
        statMonitor.incPuts(StoreType.REMOTE, 1L);
        syncMonitor.afterPut(key);

        localStore.put(key, toLocalStoreValue(value));
        statMonitor.incPuts(StoreType.LOCAL, 1L);
    }

    @Override
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        int size = keyValues.size();

        Map<String, byte[]> remoteKeyValues = Maps.newHashMap(size);
        keyValues.forEach((k, v) -> remoteKeyValues.put(k, toRemoteStoreValue(v)));
        remoteStore.putAll(remoteKeyValues);

        statMonitor.incPuts(StoreType.REMOTE, size);
        syncMonitor.afterPutAll(keyValues.keySet());

        Map<String, Object> localKeyValues = Maps.newHashMap(size);
        keyValues.forEach((k, v) -> localKeyValues.put(k, toLocalStoreValue(v)));
        localStore.putAll(localKeyValues);

        statMonitor.incPuts(StoreType.LOCAL, size);
    }

    @Override
    protected void doEvict(String key) {
        remoteStore.evict(key);
        statMonitor.incRemovals(StoreType.REMOTE, 1L);

        localStore.evict(key);

        syncMonitor.afterEvict(key);
        statMonitor.incRemovals(StoreType.LOCAL, 1L);
    }

    @Override
    protected void doEvictAll(Set<String> keys) {
        int size = keys.size();

        remoteStore.evictAll(keys);
        statMonitor.incRemovals(StoreType.REMOTE, size);

        localStore.evictAll(keys);

        syncMonitor.afterEvictAll(keys);
        statMonitor.incRemovals(StoreType.LOCAL, size);
    }

    @Override
    public void clear() {
        remoteStore.clear();
        statMonitor.incClears(StoreType.REMOTE);

        localStore.clear();

        syncMonitor.afterClear();
        statMonitor.incClears(StoreType.LOCAL);
    }

}