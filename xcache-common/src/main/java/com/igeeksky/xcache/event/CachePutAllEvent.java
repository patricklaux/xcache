package com.igeeksky.xcache.event;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-22
 */
public class CachePutAllEvent<K, V> extends CacheEvent {

    private Map<? extends K, ? extends V> keyValues;

    public CachePutAllEvent() {
    }

    public CachePutAllEvent(Map<? extends K, ? extends V> keyValues, long millis) {
        this.keyValues = keyValues;
        this.setMillis(millis);
    }

    public Map<? extends K, ? extends V> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(Map<? extends K, ? extends V> keyValues) {
        this.keyValues = keyValues;
    }
}
