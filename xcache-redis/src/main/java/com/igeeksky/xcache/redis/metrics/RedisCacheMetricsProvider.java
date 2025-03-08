package com.igeeksky.xcache.redis.metrics;

import com.igeeksky.xcache.extension.metrics.AbstractCacheMetricsProvider;
import com.igeeksky.xcache.extension.metrics.CacheMetricsMessage;
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
public class RedisCacheMetricsProvider extends AbstractCacheMetricsProvider {

    private static final String PREFIX = "stat:";
    private final StreamPublisher<byte[], byte[], CacheMetricsMessage> publisher;

    public RedisCacheMetricsProvider(RedisMetricsConfig config) {
        super(config.getScheduler(), config.getPeriod());
        RedisCacheMetricsCodec codec = config.getCodec();
        StreamOperator<byte[], byte[]> operator = config.getOperator();
        String channel = config.isEnableGroupPrefix() ? (PREFIX + config.getGroup()) : PREFIX;
        XAddOptions options = XAddOptions.builder().maxLen(config.getMaxLen()).approximateTrimming().build();
        this.publisher = new StreamPublisher<>(codec.encodeKey(channel), options, operator, codec);
    }

    @Override
    public void publish(List<CacheMetricsMessage> messages) {
        for (CacheMetricsMessage message : messages) {
            this.publisher.publish(message);
        }
    }

}