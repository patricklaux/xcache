package com.igeeksky.xcache.extension.sync;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public interface CacheMessagePublisher {

    void publish(String channel, byte[] message);

}
