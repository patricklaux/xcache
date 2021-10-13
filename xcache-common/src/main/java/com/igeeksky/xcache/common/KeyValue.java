package com.igeeksky.xcache.common;

/**
 * @author Patrick.Lau
 * @since 0.0.1 2017-03-02 21:37:52
 */
public class KeyValue<K, V> {

    private final K key;

    private final V value;

    public KeyValue() {
        this(null, null);
    }

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public boolean hasValue() {
        return null != value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

}
