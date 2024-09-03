package com.igeeksky.xcache.extension.sync;


import com.igeeksky.xcache.common.MessagePublisher;
import com.igeeksky.xcache.props.SyncType;

import java.util.Set;

/**
 * 处理缓存更新事件
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheSyncMonitor {

    private final String sid;
    private final MessagePublisher<CacheSyncMessage> publisher;

    private boolean remove = false;
    private boolean clear = false;

    public CacheSyncMonitor(SyncConfig<?> config, MessagePublisher<CacheSyncMessage> publisher) {
        this.sid = config.getSid();
        this.publisher = publisher;
        SyncType first = config.getFirst();
        SyncType second = config.getSecond();
        if (first == SyncType.CLEAR || second == SyncType.CLEAR) {
            remove = false;
            clear = true;
        }
        if (first == SyncType.ALL || second == SyncType.ALL) {
            remove = true;
            clear = true;
        }
        if (publisher == null) {
            remove = false;
            clear = false;
        }
    }

    public void afterPut(String key) {
        if (remove) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE, key));
        }
    }

    public void afterPutAll(Set<String> keys) {
        if (remove) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE, keys));
        }
    }

    public void afterEvict(String key) {
        if (remove) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE, key));
        }
    }

    public void afterEvictAll(Set<String> keys) {
        if (remove) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE, keys));
        }
    }

    public void afterClear() {
        if (clear) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_CLEAR));
        }
    }

    public void sendMessage(CacheSyncMessage message) {
        publisher.publish(message);
    }

}