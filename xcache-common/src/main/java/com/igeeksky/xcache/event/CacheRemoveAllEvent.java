package com.igeeksky.xcache.event;

import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-24
 */
public class CacheRemoveAllEvent<K> extends CacheEvent {

    Set<? extends K> keys;

    public CacheRemoveAllEvent() {
    }

    public CacheRemoveAllEvent(Set<? extends K> keys) {
        this.keys = keys;
    }

    public Set<? extends K> getKeys() {
        return keys;
    }

    public void setKeys(Set<? extends K> keys) {
        this.keys = keys;
    }
}
