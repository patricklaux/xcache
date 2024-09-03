package com.igeeksky.xcache.common;

/**
 * 消息发布者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public interface MessagePublisher<T> {

    /**
     * @param message 消息
     */
    void publish(T message);

}
