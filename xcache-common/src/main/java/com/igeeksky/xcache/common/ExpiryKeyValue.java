package com.igeeksky.xcache.common;

import java.time.Duration;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-18
 */
public class ExpiryKeyValue<K, V> extends KeyValue<K, V> {

    private final Duration ttl;

    public ExpiryKeyValue(K key, V value, Duration ttl) {
        super(key, value);
        this.ttl = ttl;
    }

    public Duration getTtl() {
        return ttl;
    }

}
