package com.igeeksky.xcache.common;

/**
 * @author Patrick.Lau
 * since 0.0.3 2021-07-29
 */
public class CacheValue<V> {

    private final V value;

    public CacheValue(V value) {
        this.value = value;
    }

    public V getValue() {
        return this.value;
    }

    public boolean hasValue() {
        return this.value != null;
    }

}