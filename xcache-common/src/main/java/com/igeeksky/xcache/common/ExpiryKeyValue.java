package com.igeeksky.xcache.common;

/**
 * @param <K> keyType
 * @param <V> valueType
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-18
 */
public class ExpiryKeyValue<K, V> extends KeyValue<K, V> {

    private final long ttl;

    /**
     * @param key   键
     * @param value 值
     * @param ttl   存活时长, type: milliseconds
     */
    public ExpiryKeyValue(K key, V value, long ttl) {
        super(key, value);
        this.ttl = ttl;
    }

    /**
     * @return time to live, type: milliseconds
     */
    public long getTtl() {
        return ttl;
    }

}
