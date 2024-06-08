package com.igeeksky.xcache.extension;

/**
 * 广播消息的消费者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public interface CacheMessageConsumer {

    void onMessage(byte[] message);

}
