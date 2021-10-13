package com.igeeksky.xcache.extension.serialization;

import com.igeeksky.xcache.event.*;
import com.igeeksky.xcache.extension.update.CacheUpdateMessage;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
public interface CacheEventSerializer<K, V> {

    byte[] serialize(CacheUpdateMessage message);

    CacheUpdateMessage deserialize(byte[] bytes);

    String serializeLoadEvent(CacheLoadEvent<K, V> event);

    CacheLoadEvent<K, V> deserializeLoadEvent(String event);

    String serializeLoadAllEvent(CacheLoadAllEvent<K, V> event);

    CacheLoadAllEvent<K, V> deserializeLoadAllEvent(String event);

    String serializePutEvent(CachePutEvent<K, V> event);

    CachePutEvent<K, V> deserializePutEvent(String event);

    String serializePutAllEvent(CachePutAllEvent<K, V> event);

    CachePutAllEvent<K, V> deserializePutAllEvent(String event);

    String serializeRemoveEvent(CacheRemoveEvent<K> event);

    CacheRemoveEvent<K> deserializeRemoveEvent(String event);

    String serializeRemoveAllEvent(CacheRemoveAllEvent<K> event);

    CacheRemoveAllEvent<K> deserializeRemoveAllEvent(String event);

    String serializeClearEvent(CacheClearEvent event);

    CacheClearEvent deserializeClearEvent(String event);
}
