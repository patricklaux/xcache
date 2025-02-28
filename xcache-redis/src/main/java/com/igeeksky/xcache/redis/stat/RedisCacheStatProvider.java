package com.igeeksky.xcache.redis.stat;

import com.igeeksky.xcache.extension.stat.AbstractCacheStatProvider;
import com.igeeksky.xcache.extension.stat.CacheStatMessage;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.StreamPublisher;
import com.igeeksky.xredis.common.stream.XAddOptions;

import java.util.List;

/**
 * 缓存指标统计实现类
 * <p>
 * 采用 Redis Stream 实现缓存指标信息发布。
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/18
 */
public class RedisCacheStatProvider extends AbstractCacheStatProvider {

    private static final String PREFIX = "stat:";
    private final StreamPublisher<byte[], byte[], CacheStatMessage> publisher;

    public RedisCacheStatProvider(RedisStatConfig config) {
        super(config.getScheduler(), config.getPeriod());
        RedisCacheStatMessageCodec codec = config.getCodec();
        StreamOperator<byte[], byte[]> operator = config.getOperator();
        String channel = config.isEnableGroupPrefix() ? (PREFIX + config.getGroup()) : PREFIX;
        XAddOptions options = XAddOptions.builder().maxLen(config.getMaxLen()).approximateTrimming().build();
        this.publisher = new StreamPublisher<>(codec.encodeKey(channel), options, operator, codec);
    }

    @Override
    public void publish(List<CacheStatMessage> messages) {
        for (CacheStatMessage message : messages) {
            this.publisher.publish(message);
        }
    }

}