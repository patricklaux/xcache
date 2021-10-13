package com.igeeksky.xcache.extension.update;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.event.*;
import com.igeeksky.xcache.extension.serialization.CacheEventSerializer;
import com.igeeksky.xcache.util.BytesUtils;
import com.igeeksky.xcache.util.CollectionUtils;
import com.igeeksky.xcache.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-24
 */
public class CacheUpdatePolicy<K, V> implements CacheEventPolicy<K, V> {

    private final Cache<K, V> cache;
    private final CacheUpdateType cacheUpdateType;
    private final CacheEventSerializer<K, V> serializer;

    public CacheUpdatePolicy(Cache<K, V> cache, CacheUpdateType cacheUpdateType, CacheEventSerializer<K, V> serializer) {
        this.cache = cache;
        this.cacheUpdateType = cacheUpdateType;
        this.serializer = serializer;
    }

    @Override
    public void onMessage(byte[] msgBytes) {
        if (BytesUtils.isEmpty(msgBytes)) {
            return;
        }
        CacheUpdateMessage msg = serializer.deserialize(msgBytes);
        if (null != msg) {
            onMessage(msg);
        }
    }

    private void onMessage(CacheUpdateMessage msg) {
        String eventStr = msg.getEvent();
        if (StringUtils.isEmpty(eventStr)) {
            return;
        }

        CacheEventType eventType = msg.getType();
        switch (eventType) {
            case LOAD: {
                CacheLoadEvent<K, V> event = serializer.deserializeLoadEvent(eventStr);
                this.onLoadEvent(event);
                return;
            }
            case LOAD_ALL: {
                CacheLoadAllEvent<K, V> event = serializer.deserializeLoadAllEvent(eventStr);
                this.onLoadAllEvent(event);
                return;
            }
            case PUT: {
                CachePutEvent<K, V> event = serializer.deserializePutEvent(eventStr);
                this.onPutEvent(event);
                return;
            }
            case PUT_ALL: {
                CachePutAllEvent<K, V> event = serializer.deserializePutAllEvent(eventStr);
                this.onPutAllEvent(event);
                return;
            }
            case REMOVE: {
                CacheRemoveEvent<K> event = serializer.deserializeRemoveEvent(eventStr);
                this.onRemoveEvent(event);
                return;
            }
            case REMOVE_ALL: {
                CacheRemoveAllEvent<K> event = serializer.deserializeRemoveAllEvent(eventStr);
                this.onRemoveAllEvent(event);
                return;
            }
            case CLEAR: {
                CacheClearEvent event = serializer.deserializeClearEvent(eventStr);
                this.onClearEvent(event);
            }
        }
    }

    private void onLoadEvent(CacheLoadEvent<K, V> event) {
        if (CacheUpdateType.PUT == cacheUpdateType) {
            cache.put(event.getKey(), Mono.justOrEmpty(event.getValue()));
        } else {
            cache.remove(event.getKey());
        }
    }

    private void onLoadAllEvent(CacheLoadAllEvent<K, V> event) {
        Map<? extends K, ? extends V> keyValues = event.getKeyValues();
        if (CollectionUtils.isEmpty(keyValues)) {
            return;
        }

        if (CacheUpdateType.PUT == cacheUpdateType) {
            cache.putAll(Mono.justOrEmpty(keyValues));
        } else {
            cache.removeAll(keyValues.keySet());
        }
    }

    private void onPutEvent(CachePutEvent<K, V> event) {
        if (CacheUpdateType.PUT == cacheUpdateType) {
            cache.put(event.getKey(), Mono.justOrEmpty(event.getValue()));
        } else {
            cache.remove(event.getKey());
        }
    }

    private void onPutAllEvent(CachePutAllEvent<K, V> event) {
        Map<? extends K, ? extends V> keyValues = event.getKeyValues();
        if (CollectionUtils.isEmpty(keyValues)) {
            return;
        }

        if (CacheUpdateType.PUT == cacheUpdateType) {
            cache.putAll(Mono.justOrEmpty(keyValues));
        } else {
            cache.removeAll(keyValues.keySet());
        }
    }

    private void onRemoveEvent(CacheRemoveEvent<K> event) {
        cache.remove(event.getKey());
    }

    private void onRemoveAllEvent(CacheRemoveAllEvent<K> event) {
        cache.removeAll(event.getKeys());
    }

    private void onClearEvent(CacheClearEvent event) {
        cache.clear();
    }

}
