package com.igeeksky.xcache.event;

import com.igeeksky.xcache.common.CacheValue;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-24
 */
public class CacheGetEvent<K, V> extends CacheEvent {

    private K key;

    private CacheValue<V> cacheValue;

    public CacheGetEvent() {
    }

    public CacheGetEvent(K key, CacheValue<V> cacheValue, long millis) {
        this.key = key;
        this.cacheValue = cacheValue;
        this.setMillis(millis);
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public CacheValue<V> getCacheValue() {
        return cacheValue;
    }

    public void setCacheValue(CacheValue<V> cacheValue) {
        this.cacheValue = cacheValue;
    }
}
