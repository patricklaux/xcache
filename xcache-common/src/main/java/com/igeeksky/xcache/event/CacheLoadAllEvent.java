package com.igeeksky.xcache.event;

import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-24
 */
public class CacheLoadAllEvent<K, V> extends CacheEvent {

    private Set<K> keys;

    public CacheLoadAllEvent() {
    }

    public CacheLoadAllEvent(Set<K> keys) {
        this.keys = keys;
    }

    public Set<K> getKeys() {
        return keys;
    }

    public void setKeys(Set<K> keys) {
        this.keys = keys;
    }
}
