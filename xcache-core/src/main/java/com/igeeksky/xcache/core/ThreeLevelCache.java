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
public class ThreeLevelCache<K, V> extends AbstractCache<K, V> {

    private final Store<V> first;

    private final Store<V> second;

    private final Store<V> third;

    private final CacheSyncMonitor syncMonitor;

    public ThreeLevelCache(CacheConfig<K, V> config, ExtendConfig<K, V> extend, Store<V>[] stores) {
        super(config, extend);
        this.syncMonitor = extend.getSyncMonitor();
        CacheStatMonitor statMonitor = extend.getStatMonitor();
        this.first = new StoreProxy<>(stores[0], StoreLevel.FIRST, statMonitor);
        this.second = new StoreProxy<>(stores[1], StoreLevel.SECOND, statMonitor);
        this.third = new StoreProxy<>(stores[2], StoreLevel.THIRD, statMonitor);
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
            return cacheValue;
        }

        cacheValue = third.get(key);
        if (cacheValue != null) {
            second.put(key, cacheValue.getValue());
            first.put(key, cacheValue.getValue());
        }

        return cacheValue;
    }

    @Override
    protected Map<String, CacheValue<V>> doGetAll(Set<String> keys) {
        Set<String> tempKeys = new HashSet<>(keys);

        Map<String, CacheValue<V>> result = Maps.newHashMap(tempKeys.size());

        // 1. 从一级缓存读取数据
        Map<String, CacheValue<V>> firstGetAll = first.getAll(tempKeys);
        if (Maps.isNotEmpty(firstGetAll)) {
            // 1.1 一级缓存数据存入最终结果
            result.putAll(firstGetAll);

            tempKeys.removeAll(firstGetAll.keySet());
            if (tempKeys.isEmpty()) {
                return result;
            }
        }

        // 2. 从二级缓存读取未命中数据
        Map<String, CacheValue<V>> secondGetAll = second.getAll(tempKeys);
        if (Maps.isNotEmpty(secondGetAll)) {
            // 2.1 二级缓存数据存入最终结果
            result.putAll(secondGetAll);

            // 2.2 二级缓存数据保存到一级缓存
            Map<String, V> saveToLower = Maps.newHashMap(secondGetAll.size());
            secondGetAll.forEach((key, cacheValue) -> {
                tempKeys.remove(key);
                saveToLower.put(key, cacheValue.getValue());
            });
            first.putAll(saveToLower);

            if (tempKeys.isEmpty()) {
                return result;
            }
        }

        // 3. 从三级缓存读取未命中数据
        Map<String, CacheValue<V>> thirdGetAll = third.getAll(tempKeys);
        if (Maps.isNotEmpty(thirdGetAll)) {
            // 3.1 三级缓存数据存入最终结果
            result.putAll(thirdGetAll);

            // 3.2 三级缓存数据保存到一、二级缓存
            Map<String, V> saveToLower = Maps.newHashMap(thirdGetAll.size());
            secondGetAll.forEach((key, cacheValue) -> saveToLower.put(key, cacheValue.getValue()));
            second.putAll(saveToLower);
            first.putAll(saveToLower);
        }

        return result;
    }

    @Override
    protected void doPut(String key, V value) {
        third.put(key, value);
        second.put(key, value);
        first.put(key, value);
        syncMonitor.afterPut(key);
    }

    @Override
    protected void doPutAll(Map<String, ? extends V> keyValues) {
        third.putAll(keyValues);
        second.putAll(keyValues);
        first.putAll(keyValues);
        syncMonitor.afterPutAll(keyValues.keySet());
    }

    @Override
    protected void doEvict(String key) {
        third.evict(key);
        second.evict(key);
        first.evict(key);
        syncMonitor.afterEvict(key);
    }

    @Override
    protected void doEvictAll(Set<String> keys) {
        third.evictAll(keys);
        second.evictAll(keys);
        first.evictAll(keys);
        syncMonitor.afterEvictAll(keys);
    }

    @Override
    public void clear() {
        third.clear();
        second.clear();
        first.clear();
        syncMonitor.afterClear();
    }

}