package com.igeeksky.xcache.event;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-10
 */
public class CachePutNullEvent<K> extends CacheEvent {

    private K key;

    public CachePutNullEvent() {
    }

    public CachePutNullEvent(K key) {
        this.key = key;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }
}
