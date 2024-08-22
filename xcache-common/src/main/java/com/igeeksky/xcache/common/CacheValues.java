package com.igeeksky.xcache.common;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-29
 */
@SuppressWarnings("unchecked")
public abstract class CacheValues {

    public static final CacheValue<?> EMPTY_CACHE_VALUE = new CacheValue<>(null);

    private CacheValues() {
    }

    /**
     * 返回的是全局唯一的静态对象
     *
     * @param <V> 值类型
     * @return 全局唯一的静态对象（值为空）
     */
    public static <V> CacheValue<V> emptyCacheValue() {
        return (CacheValue<V>) EMPTY_CACHE_VALUE;
    }

    public static <V> CacheValue<V> newCacheValue(V value) {
        return new CacheValue<>(value);
    }

    public static <V> ExpiryCacheValue<V> newEmptyExpiryCacheValue() {
        return new ExpiryCacheValue<>(null);
    }

    public static <V> ExpiryCacheValue<V> newExpiryCacheValue(V value) {
        return new ExpiryCacheValue<>(value);
    }

    public static <V> ExpiryCacheValue<V> newExpiryCacheValue(V value, long expiryTime) {
        return new ExpiryCacheValue<>(value, expiryTime);
    }

}
