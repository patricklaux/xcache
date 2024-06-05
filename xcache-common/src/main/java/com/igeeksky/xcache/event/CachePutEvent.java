package com.igeeksky.xcache.event;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-22
 */
public class CachePutEvent<K, V> extends CacheEvent {

    private K key;

    public CachePutEvent() {
    }

    public CachePutEvent(K key) {
        this.key = key;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }
}
