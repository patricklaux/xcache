package com.igeeksky.xcache.extension.monitor;


import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;

import java.util.*;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-20
 */
public class CacheMonitorProxy<K, V> implements CacheMonitor<K, V> {

    private final List<CacheMonitor<K, V>> cacheMonitors = new ArrayList<>();

    public void addCacheMonitors(Collection<CacheMonitor<K, V>> cacheMonitors) {
        if (null != cacheMonitors) {
            this.cacheMonitors.addAll(cacheMonitors);
        }
    }

    @Override
    public void afterGet(K key, CacheValue<V> cacheValue) {
        cacheMonitors.forEach(monitor -> monitor.afterGet(key, cacheValue));
    }

    @Override
    public void afterGetAll(Collection<KeyValue<K, CacheValue<V>>> results) {
        cacheMonitors.forEach(monitor -> monitor.afterGetAll(results));
    }

    @Override
    public void afterLoad(K key, V value) {
        cacheMonitors.forEach(monitor -> monitor.afterLoad(key, value));
    }

    @Override
    public void afterLoadAll(Map<? extends K, ? extends V> keyValues) {
        cacheMonitors.forEach(monitor -> monitor.afterLoadAll(keyValues));
    }

    @Override
    public void afterPut(K key, V value) {
        cacheMonitors.forEach(monitor -> monitor.afterPut(key, value));
    }

    @Override
    public void afterPutAll(Map<? extends K, ? extends V> keyValues) {
        cacheMonitors.forEach(monitor -> monitor.afterPutAll(keyValues));
    }

    @Override
    public void afterRemove(K key) {
        cacheMonitors.forEach(monitor -> monitor.afterRemove(key));
    }

    @Override
    public void afterRemoveAll(Set<? extends K> keys) {
        cacheMonitors.forEach(monitor -> monitor.afterRemoveAll(keys));
    }

    @Override
    public void afterClear() {
        cacheMonitors.forEach(CacheMonitor::afterClear);
    }

}
