package com.igeeksky.xcache.redis;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.stream.AddOptions;
import com.igeeksky.xcache.common.MessagePublisher;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class StreamMessagePublisher<T> implements MessagePublisher<T> {

    private final byte[] channel;
    private final AddOptions options;
    private final RedisOperator operator;
    private final StreamMessageCodec<T> codec;

    public StreamMessagePublisher(RedisOperator operator, long maxLen, String channel, StreamMessageCodec<T> codec) {
        this.operator = operator;
        this.options = AddOptions.builder().maxLen(maxLen).approximateTrimming().build();
        this.codec = codec;
        this.channel = codec.encodeKey(channel);
    }

    @Override
    public void publish(T message) {
        operator.xadd(channel, options, codec.encodeMsg(message));
    }

}