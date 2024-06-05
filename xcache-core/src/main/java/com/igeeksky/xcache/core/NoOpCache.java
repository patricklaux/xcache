package com.igeeksky.xcache.core;


import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.StoreType;
import com.igeeksky.xcache.core.config.CacheConfig;
import com.igeeksky.xcache.extension.loader.CacheLoader;
import com.igeeksky.xcache.extension.statistic.CacheStatMonitor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 无操作缓存
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-19
 */
public class NoOpCache<K, V> extends AbstractCache<K, V> {

    private final CacheStatMonitor statMonitor;

    public NoOpCache(CacheConfig<K, V> config) {
        super(config);
        this.statMonitor = config.getStatMonitor();
    }

    @Override
    protected CacheValue<V> doGet(String key) {
        statMonitor.incMisses(StoreType.NOOP, 1L);
        return null;
    }

    @Override
    protected V doLoad(K key, String storeKey, CacheLoader<K, V> cacheLoader) {
        V value = cacheLoader.load(key);
        statMonitor.incLoads();
        return value;
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        statMonitor.incMisses(StoreType.NOOP, keys.size());
        return Collections.emptyMap();
    }

    @Override
    protected void doPut(String key, V value) {
        statMonitor.incPuts(StoreType.NOOP, 1L);
    }

    @Override
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        statMonitor.incPuts(StoreType.NOOP, keyValues.size());
    }

    @Override
    protected void doEvict(String key) {
        statMonitor.incRemovals(StoreType.NOOP, 1L);
    }

    @Override
    protected void doEvictAll(Set<String> keys) {
        statMonitor.incRemovals(StoreType.NOOP, keys.size());
    }

    @Override
    public void clear() {
        statMonitor.incClears(StoreType.NOOP);
    }

}
