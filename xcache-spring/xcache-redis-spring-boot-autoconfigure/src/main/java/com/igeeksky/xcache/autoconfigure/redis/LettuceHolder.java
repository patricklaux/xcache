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
import io.lettuce.core.codec.ByteArrayCodec;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Lettuce 对象持有者
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceHolder {

    private static final ByteArrayCodec CODEC = ByteArrayCodec.INSTANCE;

    private final RedisOperatorFactory factory;

    private final StreamOptions streamOptions;

    private final RedisStatOptions statOptions;

    private final RedisSyncOptions syncOptions;

    private final SingletonSupplier<LettuceOperatorProxy> redisOperatorProxySupplier;

    private final SingletonSupplier<RedisOperator<byte[], byte[]>> redisOperatorSupplier;

    private final SingletonSupplier<StreamOperator<byte[], byte[]>> streamOperatorSupplier;

    private final SingletonSupplier<StreamContainer<byte[], byte[]>> streamContainerSupplier;

    public LettuceHolder(LettuceGenericConfig genericConfig, LettuceConfig lettuceConfig,
                         RedisOperatorFactory factory, ScheduledExecutorService scheduler) {
        this.factory = factory;
        this.statOptions = (lettuceConfig.getStat() != null) ? lettuceConfig.getStat() : new RedisStatOptions();
        this.syncOptions = (lettuceConfig.getSync() != null) ? lettuceConfig.getSync() : new RedisSyncOptions();
        this.streamOptions = (lettuceConfig.getStream() != null) ? lettuceConfig.getStream() : new StreamOptions();

        long timeout = genericConfig.getTimeout();
        int batchSize = lettuceConfig.getBatchSize();

        this.redisOperatorSupplier = SingletonSupplier.of(() -> factory.redisOperator(CODEC));
        this.redisOperatorProxySupplier = SingletonSupplier.of(() -> {
            RedisOperator<byte[], byte[]> redisOperator = this.redisOperatorSupplier.get();
            return new LettuceOperatorProxy(timeout, batchSize, redisOperator);
        });
        this.streamOperatorSupplier = SingletonSupplier.of(() -> {
            RedisOperator<byte[], byte[]> redisOperator = this.redisOperatorSupplier.get();
            return new LettuceStreamOperator<>(redisOperator);
        });
        this.streamContainerSupplier = SingletonSupplier.of(() -> {
            Long block = (streamOptions.getBlock() < 0) ? null : streamOptions.getBlock();
            ReadOptions readOptions = ReadOptions.from(block, streamOptions.getCount());
            return factory.streamContainer(CODEC, scheduler, streamOptions.getInterval(), readOptions);
        });
    }

    public RedisOperatorFactory getFactory() {
        return factory;
    }

    public RedisStatOptions getStatOptions() {
        return statOptions;
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
        RedisOperator<byte[], byte[]> redisOperator = redisOperatorSupplier.getIfPresent();
        StreamContainer<byte[], byte[]> container = streamContainerSupplier.getIfPresent();

        CompletableFuture<Void> future1 = (container != null) ? container.shutdownAsync()
                : CompletableFuture.completedFuture(null);
        CompletableFuture<Void> future2 = (redisOperator != null) ? redisOperator.closeAsync()
                : CompletableFuture.completedFuture(null);
        future1.thenCompose(ignored -> future2)
                .thenAccept(ignored -> {
                    RedisOperatorFactory factory = this.factory;
                    if (factory != null) {
                        factory.shutdown();
                    }
                }).join();
    }

}
