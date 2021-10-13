package com.igeeksky.xcache.extension.update;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-11
 */
public interface CacheUpdatePublisher {

    void publish(byte[] message);

}
