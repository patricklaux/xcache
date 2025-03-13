package com.igeeksky.xcache.common;

/**
 * 缓存值包装类
 *
 * @param <V> 值类型
 * @author Patrick.Lau
 * since 0.0.3 2021-07-29
 */
public class CacheValue<V> {

    private static final CacheValue<?> EMPTY_CACHE_VALUE = new CacheValue<>(null);

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

    @SuppressWarnings("unchecked")
    public static <V> CacheValue<V> empty() {
        return (CacheValue<V>) EMPTY_CACHE_VALUE;
    }

    public static <V> CacheValue<V> create(V value) {
        return new CacheValue<>(value);
    }

    @Override
    public String toString() {
        return (value == null) ? "{}" : "{\"value\":" + value + "}";
    }

}