package com.igeeksky.xcache.extension.sync;

import com.igeeksky.xcache.extension.CacheMessageConsumer;
import com.igeeksky.xcache.extension.serializer.Serializer;
import com.igeeksky.xcache.store.LocalStore;
import com.igeeksky.xtool.core.collection.CollectionUtils;

import java.util.Objects;
import java.util.Set;

/**
 * <P>监听缓存广播消息</P>
 * 根据不同 event，删除本地缓存中 key 对应的 value，或清空本地缓存的所有数据。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheSyncConsumer implements CacheMessageConsumer {

    private final String sid;

    private final LocalStore localCacheStore;

    private final Serializer<CacheSyncMessage> serializer;

    public CacheSyncConsumer(String sid, LocalStore localCacheStore, Serializer<CacheSyncMessage> serializer) {
        this.sid = sid;
        this.localCacheStore = localCacheStore;
        this.serializer = serializer;
    }

    public void onMessage(byte[] source) {
        CacheSyncMessage message = serializer.deserialize(source);
        String sourceId = message.getSid();
        if (Objects.equals(sid, sourceId)) {
            return;
        }
        int type = message.getType();
        if (Objects.equals(CacheSyncMessage.TYPE_REMOVE, type)) {
            Set<String> keys = message.getKeys();
            if (CollectionUtils.isNotEmpty(keys)) {
                localCacheStore.evictAll(keys);
            }
            return;
        }
        if (Objects.equals(CacheSyncMessage.TYPE_CLEAR, type)) {
            localCacheStore.clear();
        }
    }

}
