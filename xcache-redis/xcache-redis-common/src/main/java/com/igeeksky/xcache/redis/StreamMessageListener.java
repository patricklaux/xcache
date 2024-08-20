package com.igeeksky.xcache.redis;

import com.igeeksky.redis.stream.StreamMessage;
import com.igeeksky.xcache.core.MessageListener;

import java.util.function.Consumer;

/**
 * 流消息监听适配类
 * <p>
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/23
 */
public record StreamMessageListener(MessageListener listener) implements Consumer<StreamMessage> {

    /**
     * 将 StreamMessage 转换为 MessageListener 可监听的消息格式，
     * 并将该消息发送给指定的 MessageListener 进行处理。
     *
     * @param message 流消息
     */
    @Override
    public void accept(StreamMessage message) {
        listener.onMessage(message.body());
    }

}