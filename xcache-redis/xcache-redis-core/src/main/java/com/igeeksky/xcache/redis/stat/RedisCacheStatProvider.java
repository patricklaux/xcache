package com.igeeksky.xcache.redis.stat;

import com.igeeksky.xcache.extension.stat.AbstractCacheStatProvider;
import com.igeeksky.xcache.extension.stat.CacheStatMessage;
import com.igeeksky.xcache.extension.stat.StatMessageCodec;
import com.igeeksky.xcache.redis.StreamMessagePublisher;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/18
 */
public class RedisCacheStatProvider extends AbstractCacheStatProvider {

    private static final String PREFIX = "stat:";

    private final StreamMessagePublisher publisher;
    private final StatMessageCodec codec;
    private final byte[] channel;

    public RedisCacheStatProvider(RedisStatConfig config) {
        super(config.getScheduler(), config.getPeriod());
        this.publisher = new StreamMessagePublisher(config.getOperator(), config.getMaxLen());
        this.codec = new StatMessageCodec(null);
        this.channel = this.codec.encode(PREFIX + config.getSuffix());
    }

    @Override
    public void publish(List<CacheStatMessage> messages) {
        for (CacheStatMessage message : messages) {
            this.publisher.publish(this.channel, this.codec.encodeMsg(message));
        }
    }

}