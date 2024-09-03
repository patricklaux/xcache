package com.igeeksky.xcache.extension.sync;

import com.igeeksky.xcache.common.MessageListener;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.props.SyncType;
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

    private final Store<V> first;
    private final Store<V> second;

    private final boolean sync;
    private final boolean firstClear;
    private final boolean secondClear;
    private final boolean firstRemove;
    private final boolean secondRemove;

    public SyncMessageListener(SyncConfig<V> config) {
        this.sid = config.getSid();
        this.first = config.getFirstStore();
        this.second = config.getSecondStore();
        this.firstClear = isClear(first, config.getFirst());
        this.secondClear = isClear(second, config.getSecond());
        this.firstRemove = isRemove(first, config.getFirst());
        this.secondRemove = isRemove(second, config.getSecond());
        this.sync = firstClear || secondClear;
    }

    public void onMessage(CacheSyncMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("onMessage: {}", message);
        }

        if (!sync) return;

        String sourceId = message.getSid();
        if (Objects.equals(sid, sourceId)) {
            return;
        }

        int type = message.getType();
        if (Objects.equals(CacheSyncMessage.TYPE_REMOVE, type)) {
            Set<String> keys = message.getKeys();
            if (CollectionUtils.isNotEmpty(keys)) {
                if (firstRemove) first.evictAll(keys);
                if (secondRemove) second.evictAll(keys);
            }
            return;
        }

        if (Objects.equals(CacheSyncMessage.TYPE_CLEAR, type)) {
            if (firstClear) first.clear();
            if (secondClear) second.clear();
        }
    }

    private static <V> boolean isRemove(Store<V> store, SyncType type) {
        return store != null && Objects.equals(SyncType.ALL, type);
    }

    private static <V> boolean isClear(Store<V> store, SyncType type) {
        return store != null && (Objects.equals(SyncType.ALL, type) || Objects.equals(SyncType.CLEAR, type));
    }

}