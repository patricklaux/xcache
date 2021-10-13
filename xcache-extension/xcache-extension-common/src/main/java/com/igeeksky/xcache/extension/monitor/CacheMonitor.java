package com.igeeksky.xcache.extension.monitor;


import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-06
 */
public interface CacheMonitor<K, V> {

    default void afterGet(K key, CacheValue<V> cacheValue) {
    }

    default void afterGetAll(Collection<KeyValue<K, CacheValue<V>>> results) {
    }

    default void afterLoad(K key, V value) {
    }

    default void afterLoadAll(Map<? extends K, ? extends V> keyValues) {
    }

    default void afterPut(K key, V value) {
    }

    default void afterPutAll(Map<? extends K, ? extends V> keyValues) {
    }

    default void afterRemove(K key) {
    }

    default void afterRemoveAll(Set<? extends K> keys) {
    }

    default void afterClear() {
    }

}
