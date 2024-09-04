package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheWriter;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/4
 */
public class CacheWriterProxy<K, V> implements CacheWriter<K, V> {

    private final CacheWriter<K, V> cacheWriter;

    public CacheWriterProxy(CacheWriter<K, V> cacheWriter) {
        this.cacheWriter = cacheWriter;
    }

    @Override
    public void delete(K key) {
        if (cacheWriter != null) {
            cacheWriter.delete(key);
        }
    }

    @Override
    public void deleteAll(Set<? extends K> keys) {
        if (cacheWriter != null) {
            cacheWriter.deleteAll(keys);
        }
    }

    @Override
    public void write(K key, V value) {
        if (cacheWriter != null) {
            cacheWriter.write(key, value);
        }
    }

    @Override
    public void writeAll(Map<? extends K, ? extends V> keyValues) {
        if (cacheWriter != null) {
            cacheWriter.writeAll(keyValues);
        }
    }

}
