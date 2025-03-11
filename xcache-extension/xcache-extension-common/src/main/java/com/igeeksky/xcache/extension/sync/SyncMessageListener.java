package com.igeeksky.xcache.extension.sync;

import com.igeeksky.xcache.common.MessageListener;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

/**
 * <P>监听缓存广播消息</P>
 * 根据不同 event，删除本地缓存中 key 对应的 value，或清空本地缓存的所有数据。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class SyncMessageListener<V> implements MessageListener<CacheSyncMessage> {

    private static final Logger log = LoggerFactory.getLogger(SyncMessageListener.class);

    private final String sid;

    private final Store<V> firstStore;
    private final Store<V> secondStore;

    private final boolean enabled;
    private final boolean firstEnabled;
    private final boolean secondEnabled;

    public SyncMessageListener(SyncConfig<V> config) {
        this.sid = config.getSid();
        this.firstStore = config.getFirstStore();
        this.secondStore = config.getSecondStore();
        this.firstEnabled = isEnabled(firstStore, config.getFirst());
        this.secondEnabled = isEnabled(secondStore, config.getSecond());
        this.enabled = firstEnabled || secondEnabled;
    }

    public void accept(CacheSyncMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("onMessage: {}", message);
        }

        if (!enabled) return;

        String sourceId = message.getSid();
        if (Objects.equals(sid, sourceId)) {
            return;
        }

        int type = message.getType();
        if (CacheSyncMessage.TYPE_REMOVE == type) {
            Set<String> keys = message.getKeys();
            if (CollectionUtils.isNotEmpty(keys)) {
                if (secondEnabled) {
                    secondStore.removeAll(keys);
                }
                if (firstEnabled) {
                    firstStore.removeAll(keys);
                }
            }
            return;
        }

        if (CacheSyncMessage.TYPE_CLEAR == type) {
            if (firstEnabled) firstStore.clear();
            if (secondEnabled) secondStore.clear();
            return;
        }

        log.error("onMessage: unknown message type: {}", type);
    }

    private static <V> boolean isEnabled(Store<V> store, boolean enabled) {
        return store != null && enabled;
    }

}