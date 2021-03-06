package com.igeeksky.xcache.core;

/**
 * @author Patrick.Lau
 * @date 2021-06-10
 */
public class SimpleCacheValue<V> implements CacheValue<V> {

    private final V value;

    public SimpleCacheValue(V value) {
        this.value = value;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public boolean hasValue() {
        return null != this.value;
    }
}