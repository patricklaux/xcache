package com.igeeksky.xcache.event;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-21
 */
public class CacheLoadEvent<K, V> extends CacheEvent {

    private K key;

    public CacheLoadEvent() {
    }

    public CacheLoadEvent(K key) {
        this.key = key;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }
}
