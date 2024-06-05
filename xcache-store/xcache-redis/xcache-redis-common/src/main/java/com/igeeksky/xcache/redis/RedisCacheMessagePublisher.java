package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.extension.sync.CacheMessagePublisher;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class RedisCacheMessagePublisher implements CacheMessagePublisher {

    private final RedisPubSubConnection pubSubConnection;

    public RedisCacheMessagePublisher(RedisPubSubConnection pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
    }

    @Override
    public void publish(String channel, byte[] message) {
        pubSubConnection.publish(channel, message);
    }

}