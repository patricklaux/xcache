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

/**
 * 三级组合缓存
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
@SuppressWarnings("unchecked")
public class ThreeLevelCache<K, V> extends AbstractCache<K, V> {

    private static final int LIMIT = 2;
    private static final int LENGTH = 3;

    private final CacheSyncMonitor syncMonitor;
    private final Store<V>[] stores = new Store[LENGTH];

    public ThreeLevelCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend, Store<V>[] stores) {
        super(config, extend);
        this.syncMonitor = extend.getSyncMonitor();
        CacheStatMonitor statMonitor = extend.getStatMonitor();
        StoreLevel[] levels = StoreLevel.values();
        for (int i = 0; i < LENGTH; i++) {
            this.stores[i] = new StoreProxy<>(stores[i], levels[i], statMonitor);
        }
    }

    @Override
    protected CacheValue<V> doGet(String key) {
        for (int i = 0; i < LENGTH; i++) {
            CacheValue<V> cacheValue = stores[i].get(key);
            if (cacheValue != null) {
                if (i > 0) {
                    for (int j = i - 1; j >= 0; j--) {
                        stores[j].put(key, cacheValue.getValue());
                    }
                }
                return cacheValue;
            }
        }
        return null;
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        Set<String> cloneKeys = new HashSet<>(keys);
        Map<String, CacheValue<V>> result = Maps.newHashMap(keys.size());

        Map<String, V> saveToLower = null;
        for (int i = 0; i < LENGTH; i++) {
            Store<V> store = stores[i];
            Map<String, CacheValue<V>> cacheValues = store.getAll(cloneKeys);
            if (Maps.isEmpty(cacheValues)) {
                continue;
            }

            if (i > 0) {
                saveToLower = Maps.newHashMap(cacheValues.size());
            }
            for (Map.Entry<String, CacheValue<V>> entry : cacheValues.entrySet()) {
                String key = entry.getKey();
                CacheValue<V> cacheValue = entry.getValue();
                if (cacheValue != null) {
                    result.put(key, cacheValue);
                    cloneKeys.remove(key);
                    if (saveToLower != null) {
                        saveToLower.put(key, cacheValue.getValue());
                    }
                }
            }

            if (saveToLower != null) {
                for (int j = i - 1; j >= 0; j--) {
                    stores[j].putAll(saveToLower);
                }
                saveToLower = null;
            }

            if (cloneKeys.isEmpty()) {
                return result;
            }
        }

        return result;
    }

    @Override
    protected void doPut(String key, V value) {
        for (int j = LIMIT; j >= 0; j--) {
            stores[j].put(key, value);
        }
        syncMonitor.afterPut(key);
    }

    @Override
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        for (int j = LIMIT; j >= 0; j--) {
            stores[j].putAll(keyValues);
        }
        syncMonitor.afterPutAll(keyValues.keySet());
    }

    @Override
    protected void doEvict(String key) {
        for (int j = LIMIT; j >= 0; j--) {
            stores[j].evict(key);
        }
        syncMonitor.afterEvict(key);
    }

    @Override
    protected void doEvictAll(Set<String> keys) {
        for (int j = LIMIT; j >= 0; j--) {
            stores[j].evictAll(keys);
        }
        syncMonitor.afterEvictAll(keys);
    }

    @Override
    public void clear() {
        for (int j = LIMIT; j >= 0; j--) {
            stores[j].clear();
        }
        syncMonitor.afterClear();
    }

}