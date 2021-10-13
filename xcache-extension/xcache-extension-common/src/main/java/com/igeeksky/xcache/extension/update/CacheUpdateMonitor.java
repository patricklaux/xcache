package com.igeeksky.xcache.extension.update;

import com.igeeksky.xcache.event.*;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.CacheEventSerializer;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-21
 */
public class CacheUpdateMonitor<K, V> implements CacheMonitor<K, V> {

    private final String cacheId;

    private final CacheEventSerializer<K, V> serializer;
    private final CacheUpdatePublisher publisher;

    public CacheUpdateMonitor(String cacheId, CacheEventSerializer<K, V> serializer, CacheUpdatePublisher publisher) {
        this.cacheId = cacheId;
        this.serializer = serializer;
        this.publisher = publisher;
    }

    @Override
    public void afterLoad(K key, V value) {
        CacheLoadEvent<K, V> loadEvent = new CacheLoadEvent<>(key, value, System.currentTimeMillis());
        String event = serializer.serializeLoadEvent(loadEvent);
        createAndPublish(event, CacheEventType.LOAD);
    }

    @Override
    public void afterLoadAll(Map<? extends K, ? extends V> keyValues) {
        CacheLoadAllEvent<K, V> loadAllEvent = new CacheLoadAllEvent<>(keyValues, System.currentTimeMillis());
        String event = serializer.serializeLoadAllEvent(loadAllEvent);
        createAndPublish(event, CacheEventType.LOAD_ALL);
    }

    @Override
    public void afterPut(K key, V value) {
        CachePutEvent<K, V> putEvent = new CachePutEvent<>(key, value, System.currentTimeMillis());
        String event = serializer.serializePutEvent(putEvent);
        createAndPublish(event, CacheEventType.PUT);
    }

    @Override
    public void afterPutAll(Map<? extends K, ? extends V> keyValues) {
        CachePutAllEvent<K, V> putAllEvent = new CachePutAllEvent<>(keyValues, System.currentTimeMillis());
        String event = serializer.serializePutAllEvent(putAllEvent);
        createAndPublish(event, CacheEventType.PUT_ALL);
    }

    @Override
    public void afterRemove(K key) {
        CacheRemoveEvent<K> removeEvent = new CacheRemoveEvent<>(key, System.currentTimeMillis());
        String event = serializer.serializeRemoveEvent(removeEvent);
        createAndPublish(event, CacheEventType.REMOVE);
    }

    @Override
    public void afterRemoveAll(Set<? extends K> keys) {
        CacheRemoveAllEvent<K> removeAllEvent = new CacheRemoveAllEvent<>(keys, System.currentTimeMillis());
        String event = serializer.serializeRemoveAllEvent(removeAllEvent);
        createAndPublish(event, CacheEventType.REMOVE_ALL);
    }

    @Override
    public void afterClear() {
        CacheClearEvent clearEvent = new CacheClearEvent(System.currentTimeMillis());
        String event = serializer.serializeClearEvent(clearEvent);
        createAndPublish(event, CacheEventType.CLEAR);
    }

    private void createAndPublish(String event, CacheEventType eventType) {
        CacheUpdateMessage message = new CacheUpdateMessage();
        message.setEvent(event);
        message.setType(eventType);
        message.setCacheId(cacheId);
        publisher.publish(serializer.serialize(message));
    }

}
