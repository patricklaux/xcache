package com.igeeksky.xcache.redis.sync;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.stream.ReadOffset;
import com.igeeksky.redis.stream.StreamListenerContainer;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncProvider;
import com.igeeksky.xcache.extension.sync.SyncConfig;
import com.igeeksky.xcache.extension.sync.SyncMessageListener;
import com.igeeksky.xcache.redis.StreamMessageListener;
import com.igeeksky.xcache.redis.StreamMessagePublisher;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class RedisCacheSyncProvider implements CacheSyncProvider {

    private final RedisOperator redisOperator;
    private final StreamListenerContainer listenerContainer;

    public RedisCacheSyncProvider(StreamListenerContainer listenerContainer) {
        this.redisOperator = listenerContainer.getRedisOperator();
        this.listenerContainer = listenerContainer;
    }

    /**
     * 注册消息监听器
     *
     * @param channel  消息的通道标识，用于指定订阅的特定通道。
     * @param listener 消息监听器，用于消费缓存同步消息（删除特定键集 或 清空缓存）
     * @param <V>      缓存的值泛型类型
     */
    @Override
    public <V> void register(byte[] channel, SyncMessageListener<V> listener) {
        // 获取 Redis 主机时间作为起始 ID（缓存实际启用是在此方法完成之后，因此不会遗漏消息）
        String startId = this.redisOperator.time() + "-0";
        // 使用获取的起始ID和监听器创建一个消息消费者，并将其注册到监控器中。
        this.listenerContainer.register(ReadOffset.from(channel, startId), new StreamMessageListener(listener));
    }

    @Override
    public <V> CacheSyncMonitor getMonitor(SyncConfig<V> config) {
        StreamMessagePublisher publisher = new StreamMessagePublisher(this.redisOperator, config.getMaxLen());
        return new CacheSyncMonitor(config, publisher);
    }

}