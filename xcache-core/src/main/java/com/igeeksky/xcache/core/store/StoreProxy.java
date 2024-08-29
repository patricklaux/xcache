package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xcache.props.StoreLevel;

import java.util.Map;
import java.util.Set;

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
        if (this.store != null) {
            this.statMonitor.setCounter(level);
        }
    }

    @Override
    public CacheValue<V> get(String key) {
        if (store == null) {
            return null;
        }
        CacheValue<V> value = store.get(key);
        if (value != null) {
            statMonitor.incHits(level, 1L);
        } else {
            statMonitor.incMisses(level, 1L);
        }
        return value;
    }

    @Override
    public Map<String, CacheValue<V>> getAll(Set<? extends String> keys) {
        if (store == null) {
            return null;
        }
        int total = keys.size();
        Map<String, CacheValue<V>> result = store.getAll(keys);

        int hits = result.size();
        if (hits > 0) {
            statMonitor.incHits(level, hits);
        }
        int misses = total - hits;
        if (misses > 0) {
            statMonitor.incMisses(level, total - hits);
        }

        return result;
    }

    @Override
    public void put(String key, V value) {
        if (store == null) {
            return;
        }
        store.put(key, value);
        statMonitor.incPuts(level, 1L);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        if (store == null) {
            return;
        }
        int size = keyValues.size();
        store.putAll(keyValues);
        if (size > 0) {
            statMonitor.incPuts(level, size);
        }
    }

    @Override
    public void evict(String key) {
        if (store == null) {
            return;
        }
        store.evict(key);
        statMonitor.incRemovals(level, 1L);
    }

    @Override
    public void evictAll(Set<? extends String> keys) {
        if (store == null) {
            return;
        }
        int size = keys.size();
        store.evictAll(keys);
        statMonitor.incRemovals(level, size);
    }

    @Override
    public void clear() {
        if (store == null) {
            return;
        }
        store.clear();
        statMonitor.incClears(level);
    }
}
