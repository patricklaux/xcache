package com.igeeksky.xcache.extension;

import com.igeeksky.xcache.common.CacheWriter;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/9
 */
public class NoopCacheWriter<K, V> implements CacheWriter<K, V> {

    private static final NoopCacheWriter<Object, Object> INSTANCE = new NoopCacheWriter<>();

    private NoopCacheWriter() {
    }

    @SuppressWarnings("unchecked")
    public static <K, V> NoopCacheWriter<K, V> getInstance() {
        return (NoopCacheWriter<K, V>) INSTANCE;
    }

    @Override
    public void delete(K key) {
    }

    @Override
    public void deleteAll(Set<? extends K> keys) {
    }

    @Override
    public void write(K key, V value) {
    }

    @Override
    public void writeAll(Map<? extends K, ? extends V> keyValues) {
    }

}
