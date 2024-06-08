package com.igeeksky.xcache.extension.sync;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.extension.CacheMessageConsumer;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public interface CacheSyncManager extends Provider {

    CacheMessagePublisher getPublisher(String channel);

    void register(String channel, CacheMessageConsumer consumer);

}
