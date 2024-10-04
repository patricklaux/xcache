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
        // 复制键集
        Set<String> cloneKeys = new HashSet<>(keys);
        // 最终结果集
        Map<String, CacheValue<V>> result = Maps.newHashMap(keys.size());

        // 从一级缓存查询数据
        Map<String, CacheValue<V>> firstAll = first.getAll(cloneKeys);
        if (Maps.isNotEmpty(firstAll)) {
            firstAll.forEach((key, cacheValue) -> {
                if (cacheValue != null) {
                    result.put(key, cacheValue);
                    cloneKeys.remove(key);
                }
            });

            if (cloneKeys.isEmpty()) {
                return result;
            }
        }

        // 从二级缓存查询数据
        Map<String, CacheValue<V>> secondAll = second.getAll(cloneKeys);
        if (Maps.isNotEmpty(secondAll)) {
            Map<String, V> saveToLower = Maps.newHashMap(secondAll.size());
            secondAll.forEach((key, cacheValue) -> {
                if (cacheValue != null) {
                    result.put(key, cacheValue);
                    saveToLower.put(key, cacheValue.getValue());
                }
            });
            // 二级缓存数据保存到一级缓存
            if (Maps.isNotEmpty(saveToLower)) {
                first.putAll(saveToLower);
            }
        }

        // 返回最终结果集
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