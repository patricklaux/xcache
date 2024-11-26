package com.igeeksky.xcache.extension.sync;


import com.igeeksky.xcache.common.MessagePublisher;

import java.util.Set;

/**
 * 监听并发送缓存数据更新事件
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheSyncMonitorImpl implements CacheSyncMonitor {

    private final String sid;
    private final MessagePublisher<CacheSyncMessage> publisher;

    /**
     * 是否启用数据同步
     */
    private boolean enabled = false;

    public CacheSyncMonitorImpl(SyncConfig<?> config, MessagePublisher<CacheSyncMessage> publisher) {
        this.sid = config.getSid();
        this.publisher = publisher;
        boolean first = config.getFirst();
        boolean second = config.getSecond();
        if (first || second) {
            enabled = true;
        }
        if (publisher == null) {
            enabled = false;
        }
    }

    @Override
    public void afterPut(String key) {
        if (enabled) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE, key));
        }
    }

    @Override
    public void afterPutAll(Set<String> keys) {
        if (enabled) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE, keys));
        }
    }

    @Override
    public void afterEvict(String key) {
        if (enabled) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE, key));
        }
    }

    @Override
    public void afterEvictAll(Set<String> keys) {
        if (enabled) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_REMOVE, keys));
        }
    }

    @Override
    public void afterClear() {
        if (enabled) {
            sendMessage(new CacheSyncMessage(sid, CacheSyncMessage.TYPE_CLEAR));
        }
    }

    private void sendMessage(CacheSyncMessage message) {
        publisher.publish(message);
    }

}