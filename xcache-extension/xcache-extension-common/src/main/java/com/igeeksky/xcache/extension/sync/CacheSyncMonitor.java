package com.igeeksky.xcache.extension.sync;

import com.igeeksky.xcache.common.CacheType;
import com.igeeksky.xcache.extension.serializer.Serializer;

import java.util.Set;

/**
 * 处理缓存更新事件
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheSyncMonitor {

    private final String sid;

    /**
     * sync-channel:cache-name
     */
    private final String channel;

    private final CacheType cacheType;

    private final CacheMessagePublisher publisher;

    private final Serializer<CacheSyncMessage> serializer;

    public CacheSyncMonitor(String sid, String channel, CacheType cacheType,
                            CacheMessagePublisher publisher, Serializer<CacheSyncMessage> serializer) {
        this.sid = sid;
        this.channel = channel;
        this.cacheType = cacheType;
        this.publisher = publisher;
        this.serializer = serializer;
    }

    public void afterPut(String key) {
        if (CacheType.BOTH == cacheType) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE).addKey(key));
        }
    }

    public void afterPutAll(Set<String> keys) {
        if (CacheType.BOTH == cacheType) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE).setKeys(keys));
        }
    }

    public void afterEvict(String key) {
        if (CacheType.BOTH == cacheType || CacheType.LOCAL == cacheType) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE).addKey(key));
        }
    }

    public void afterEvictAll(Set<String> keys) {
        if (CacheType.BOTH == cacheType || CacheType.LOCAL == cacheType) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE).setKeys(keys));
        }
    }

    public void afterClear() {
        if (CacheType.BOTH == cacheType || CacheType.LOCAL == cacheType) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_CLEAR));
        }
    }

    public void sendMessage(CacheSyncMessage message) {
        publisher.publish(channel, serializer.serialize(message));
    }

}
