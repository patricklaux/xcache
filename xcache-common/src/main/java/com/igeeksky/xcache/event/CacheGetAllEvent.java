package com.igeeksky.xcache.event;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-24
 */
public class CacheGetAllEvent<K, V> extends CacheEvent {

    private List<KeyValue<K, CacheValue<V>>> keyValues;

    public CacheGetAllEvent() {
    }

    public CacheGetAllEvent(List<KeyValue<K, CacheValue<V>>> keyValues, long millis) {
        this.keyValues = keyValues;
        this.setMillis(millis);
    }

    public List<KeyValue<K, CacheValue<V>>> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(List<KeyValue<K, CacheValue<V>>> keyValues) {
        this.keyValues = keyValues;
    }
}
