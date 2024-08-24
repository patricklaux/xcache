package com.igeeksky.xcache.redis;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.stream.AddOptions;
import com.igeeksky.xcache.common.MessagePublisher;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class StreamMessagePublisher implements MessagePublisher {

    private final AddOptions options;
    private final RedisOperator operator;

    public StreamMessagePublisher(RedisOperator operator, long maxLen) {
        this.operator = operator;
        this.options = AddOptions.builder().maxLen(maxLen).approximateTrimming().build();
    }

    @Override
    public void publish(byte[] channel, Map<byte[], byte[]> message) {
        operator.xadd(channel, options, message);
    }

}