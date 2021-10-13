package com.igeeksky.xcache.event;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-22
 */
public class CacheRemoveEvent<K> extends CacheEvent {

    private K key;

    public CacheRemoveEvent() {
    }

    public CacheRemoveEvent(K key, long millis) {
        this.key = key;
        this.setMillis(millis);
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }
}
