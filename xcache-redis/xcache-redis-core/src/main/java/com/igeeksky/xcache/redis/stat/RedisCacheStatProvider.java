package com.igeeksky.xcache.redis.stat;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.stat.AbstractCacheStatProvider;
import com.igeeksky.xcache.extension.stat.CacheStatMessage;
import com.igeeksky.xcache.redis.StreamMessagePublisher;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/18
 */
public class RedisCacheStatProvider extends AbstractCacheStatProvider {

    private static final String PREFIX = "stat:";
    private final StreamMessagePublisher<CacheStatMessage> publisher;

    public RedisCacheStatProvider(RedisStatConfig config) {
        super(config.getScheduler(), config.getPeriod());
        long maxLen = config.getMaxLen();
        String channel = PREFIX + config.getSuffix();
        RedisOperator operator = config.getOperator();
        RedisCacheStatMessageCodec codec = config.getCodec();
        this.publisher = new StreamMessagePublisher<>(operator, maxLen, channel, codec);
    }

    @Override
    public void publish(List<CacheStatMessage> messages) {
        for (CacheStatMessage message : messages) {
            this.publisher.publish(message);
        }
    }

}