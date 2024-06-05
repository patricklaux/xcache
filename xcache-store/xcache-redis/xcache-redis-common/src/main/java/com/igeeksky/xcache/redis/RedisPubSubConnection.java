package com.igeeksky.xcache.redis;


/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisPubSubConnection extends AutoCloseable {

    void psubscribe(String... patterns);

    void punsubscribe(String... patterns);

    void subscribe(String... channels);

    void unsubscribe(String... channels);

    void addListener(RedisPubSubListener listener);

    Long publish(String channel, byte[] message);
}
