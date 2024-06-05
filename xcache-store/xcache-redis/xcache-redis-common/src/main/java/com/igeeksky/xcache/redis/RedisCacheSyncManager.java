package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.extension.CacheMessageConsumer;
import com.igeeksky.xcache.extension.sync.CacheMessagePublisher;
import com.igeeksky.xcache.extension.sync.CacheSyncManager;
import com.igeeksky.xtool.core.io.IOUtils;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class RedisCacheSyncManager implements CacheSyncManager {

    private final RedisPubSubConnection connection;

    private final RedisCacheMessageListener listener;

    private final RedisCacheMessagePublisher publisher;

    public RedisCacheSyncManager(RedisConnectionFactory factory) {
        this.listener = new RedisCacheMessageListener();
        this.connection = factory.getPubSubConnection();
        this.connection.addListener(this.listener);
        this.publisher = new RedisCacheMessagePublisher(this.connection);
    }

    @Override
    public CacheMessagePublisher getPublisher(String channel) {
        return this.publisher;
    }

    @Override
    public void register(String channel, CacheMessageConsumer consumer) {
        this.listener.register(channel, consumer);
        this.connection.subscribe(channel);
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.connection);
    }

}