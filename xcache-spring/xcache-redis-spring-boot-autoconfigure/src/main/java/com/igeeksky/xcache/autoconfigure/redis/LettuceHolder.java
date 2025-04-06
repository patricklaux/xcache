package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.container.ReadOptions;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.lettuce.LettuceOperatorProxy;
import com.igeeksky.xredis.lettuce.LettuceStreamOperator;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.lettuce.config.LettuceGenericConfig;
import com.igeeksky.xtool.core.concurrent.Futures;
import io.lettuce.core.codec.ByteArrayCodec;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Lettuce 对象持有者
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceHolder {

    private static final ByteArrayCodec CODEC = ByteArrayCodec.INSTANCE;

    private final AtomicBoolean shutdownState = new AtomicBoolean(false);

    private final RedisOperatorFactory factory;

    private final StreamOptions streamOptions;

    private final RedisMetricsOptions metricsOptions;

    private final RedisSyncOptions syncOptions;

    private final long shutdownTimeout;

    private final SingletonSupplier<LettuceOperatorProxy> redisOperatorProxySupplier;

    private final SingletonSupplier<RedisOperator<byte[], byte[]>> redisOperatorSupplier;

    private final SingletonSupplier<StreamOperator<byte[], byte[]>> streamOperatorSupplier;

    private final SingletonSupplier<StreamContainer<byte[], byte[]>> streamContainerSupplier;

    public LettuceHolder(LettuceGenericConfig genericConfig, LettuceConfig lettuceConfig,
                         RedisOperatorFactory factory, ScheduledExecutorService scheduler) {
        this.factory = factory;
        this.shutdownTimeout = genericConfig.getShutdownTimeout();
        this.metricsOptions = (lettuceConfig.getMetrics() != null) ? lettuceConfig.getMetrics() : new RedisMetricsOptions();
        this.syncOptions = (lettuceConfig.getSync() != null) ? lettuceConfig.getSync() : new RedisSyncOptions();
        this.streamOptions = (lettuceConfig.getStream() != null) ? lettuceConfig.getStream() : new StreamOptions();

        long timeout = genericConfig.getTimeout();
        int batchSize = lettuceConfig.getBatchSize();
        boolean compatible = lettuceConfig.isCompatible();

        this.redisOperatorSupplier = SingletonSupplier.of(() -> factory.redisOperator(CODEC));
        this.redisOperatorProxySupplier = SingletonSupplier.of(() -> {
            RedisOperator<byte[], byte[]> redisOperator = this.redisOperatorSupplier.get();
            return new LettuceOperatorProxy(timeout, batchSize, compatible, redisOperator);
        });
        this.streamOperatorSupplier = SingletonSupplier.of(() -> {
            RedisOperator<byte[], byte[]> redisOperator = this.redisOperatorSupplier.get();
            return new LettuceStreamOperator<>(redisOperator);
        });
        this.streamContainerSupplier = SingletonSupplier.of(() -> {
            Long block = (streamOptions.getBlock() < 0) ? null : streamOptions.getBlock();
            ReadOptions readOptions = ReadOptions.from(block, streamOptions.getCount());
            return factory.streamContainer(CODEC, scheduler, streamOptions.getPeriod(), readOptions);
        });
    }

    public RedisOperatorFactory getFactory() {
        return factory;
    }

    public RedisMetricsOptions getMetricsOptions() {
        return metricsOptions;
    }

    public RedisSyncOptions getSyncOptions() {
        return syncOptions;
    }

    public SingletonSupplier<LettuceOperatorProxy> getRedisOperatorProxySupplier() {
        return redisOperatorProxySupplier;
    }

    public SingletonSupplier<RedisOperator<byte[], byte[]>> getRedisOperatorSupplier() {
        return redisOperatorSupplier;
    }

    public SingletonSupplier<StreamOperator<byte[], byte[]>> getStreamOperatorSupplier() {
        return streamOperatorSupplier;
    }

    public SingletonSupplier<StreamContainer<byte[], byte[]>> getStreamContainerSupplier() {
        return streamContainerSupplier;
    }

    public void shutdown() {
        if (shutdownState.compareAndSet(false, true)) {
            ArrayList<Future<?>> futures = new ArrayList<>(3);
            RedisOperator<byte[], byte[]> redisOperator = redisOperatorSupplier.getIfPresent();
            StreamContainer<byte[], byte[]> container = streamContainerSupplier.getIfPresent();
            if (container != null) {
                futures.add(container.shutdownAsync());
            }
            if (redisOperator != null) {
                futures.add(redisOperator.closeAsync());
            }
            if (factory != null) {
                futures.add(factory.shutdownAsync());
            }
            Futures.awaitAll(futures, shutdownTimeout, TimeUnit.MILLISECONDS);
        }
    }

}
