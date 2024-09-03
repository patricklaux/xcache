package com.igeeksky.xcache.redis;

import com.igeeksky.redis.stream.StreamMessage;
import com.igeeksky.xcache.common.MessageListener;

import java.util.function.Consumer;

/**
 * 流消息监听适配类
 * <p>
 *
 * @param <T> 监听的消息类型
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/23
 */
public class StreamMessageListener<T> implements Consumer<StreamMessage> {

    private final StreamMessageCodec<T> codec;
    private final MessageListener<T> listener;

    public StreamMessageListener(StreamMessageCodec<T> codec, MessageListener<T> listener) {
        this.codec = codec;
        this.listener = listener;
    }

    /**
     * StreamMessage 转换为 MessageListener 可监听的消息类型，
     * 并将该消息发送给指定的 MessageListener 进行处理。
     *
     * @param message 流消息
     */
    @Override
    public void accept(StreamMessage message) {
        T t = codec.decodeMsg(message.body());
        listener.onMessage(t);
    }

}