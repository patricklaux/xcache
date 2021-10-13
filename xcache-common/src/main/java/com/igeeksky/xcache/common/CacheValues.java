package com.igeeksky.xcache.common;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-29
 */
@SuppressWarnings("unchecked")
public abstract class CacheValues {

    public static final CacheValue<?> EMPTY_CACHE_VALUE = new CacheValue<>(null);
    public static final ExpiryCacheValue<?> EMPTY_EXPIRY_CACHE_VALUE = new ExpiryCacheValue<>(null);

    private CacheValues() {
    }

    public static <V> CacheValue<V> emptyCacheValue() {
        return (CacheValue<V>) EMPTY_CACHE_VALUE;
    }

    public static <V> CacheValue<V> newCacheValue(V value) {
        return new CacheValue<>(value);
    }

    public static <V> ExpiryCacheValue<V> emptyExpiryCacheValue() {
        return (ExpiryCacheValue<V>) EMPTY_EXPIRY_CACHE_VALUE;
    }

    public static <V> ExpiryCacheValue<V> newExpiryCacheValue(V value) {
        return new ExpiryCacheValue<>(value);
    }

    public static <V> ExpiryCacheValue<V> newExpiryCacheValue(V value, long expiryTime) {
        return new ExpiryCacheValue<>(value, expiryTime);
    }

}
