package com.igeeksky.xcache.core;

import com.igeeksky.xcache.core.store.StoreProxy;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.props.StoreLevel;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 两级组合缓存
 *
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
        CacheStatMonitor statMonitor = extend.getStatMonitor();
        AtomicInteger index = new AtomicInteger(0);
        this.first = getStore(statMonitor, stores, index);
        this.second = getStore(statMonitor, stores, index);
    }

    private Store<V> getStore(CacheStatMonitor statMonitor, Store<V>[] stores, AtomicInteger index) {
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
    protected CacheValue<V> doGet(String key) {
        CacheValue<V> cacheValue = first.get(key);
        if (cacheValue != null) {
            return cacheValue;
        }

        cacheValue = second.get(key);
        if (cacheValue != null) {
            first.put(key, cacheValue.getValue());
        }

        return cacheValue;
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        Set<String> cloneKeys = new HashSet<>(keys);

        Map<String, CacheValue<V>> result = Maps.newHashMap(cloneKeys.size());

        // 1. 从一级缓存读取数据
        Map<String, CacheValue<V>> firstGetAll = first.getAll(cloneKeys);
        if (Maps.isNotEmpty(firstGetAll)) {
            // 1.1 一级缓存数据存入最终结果
            result.putAll(firstGetAll);

            cloneKeys.removeAll(firstGetAll.keySet());
            if (cloneKeys.isEmpty()) {
                return result;
            }
        }

        // 2. 从二级缓存读取未命中数据
        Map<String, CacheValue<V>> secondGetAll = second.getAll(cloneKeys);
        if (Maps.isNotEmpty(secondGetAll)) {
            // 2.1 二级缓存数据存入最终结果
            result.putAll(secondGetAll);

            // 2.2 二级缓存数据保存到一级缓存
            Map<String, V> saveToLower = Maps.newHashMap(secondGetAll.size());
            secondGetAll.forEach((key, cacheValue) -> saveToLower.put(key, cacheValue.getValue()));
            first.putAll(saveToLower);
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
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        second.putAll(keyValues);
        first.putAll(keyValues);
        syncMonitor.afterPutAll(keyValues.keySet());
    }

    @Override
    protected void doEvict(String key) {
        second.evict(key);
        first.evict(key);
        syncMonitor.afterEvict(key);
    }

    @Override
    protected void doEvictAll(Set<String> keys) {
        second.evictAll(keys);
        first.evictAll(keys);
        syncMonitor.afterEvictAll(keys);
    }

    @Override
    public void clear() {
        second.clear();
        first.clear();
        syncMonitor.afterClear();
    }

}