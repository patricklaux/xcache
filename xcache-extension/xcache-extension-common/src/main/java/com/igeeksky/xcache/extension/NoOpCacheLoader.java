package com.igeeksky.xcache.extension;

import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/9
 */
public class NoOpCacheLoader<K, V> implements CacheLoader<K, V> {

    public static final NoOpCacheLoader<Object, Object> INSTANCE = new NoOpCacheLoader<>();

    private NoOpCacheLoader() {
    }

    @SuppressWarnings("unchecked")
    public static <K, V> NoOpCacheLoader<K, V> getInstance() {
        return (NoOpCacheLoader<K, V>) INSTANCE;
    }

    @Override
    public V load(K key) {
        return null;
    }

    @Override
    public Map<K, V> loadAll(Set<? extends K> keys) {
        return Maps.newHashMap(0);
    }

}
