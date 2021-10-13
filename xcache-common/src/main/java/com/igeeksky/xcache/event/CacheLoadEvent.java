package com.igeeksky.xcache.event;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-21
 */
public class CacheLoadEvent<K, V> extends CacheEvent {

    private K key;
    private V value;

    public CacheLoadEvent() {
    }

    public CacheLoadEvent(K key, V value, long millis) {
        this.key = key;
        this.value = value;
        this.setMillis(millis);
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

}
