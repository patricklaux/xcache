package com.igeeksky.xcache.event;

import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-22
 */
public class CachePutAllEvent<K> extends CacheEvent {

    private Set<K> keys;

    public CachePutAllEvent() {
    }

    public CachePutAllEvent(Set<K> keys) {
        this.keys = keys;
    }

    public Set<K> getKeys() {
        return keys;
    }

    public void setKeys(Set<K> keys) {
        this.keys = keys;
    }
}
