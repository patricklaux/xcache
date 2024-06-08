package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.redis.RedisPubSubConnection;
import com.igeeksky.xcache.redis.RedisPubSubListener;
import com.igeeksky.xtool.core.io.IOUtils;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.RedisClusterPubSubListener;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.cluster.pubsub.api.sync.RedisClusterPubSubCommands;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public class LettuceClusterPubSubConnection implements RedisPubSubConnection {

    private final StatefulRedisClusterPubSubConnection<String, byte[]> connection;
    private final RedisClusterPubSubCommands<String, byte[]> pubSubCommands;

    public LettuceClusterPubSubConnection(StatefulRedisClusterPubSubConnection<String, byte[]> connection) {
        this.connection = connection;
        this.pubSubCommands = connection.sync();
    }

    @Override
    public void psubscribe(String... patterns) {
        pubSubCommands.psubscribe(patterns);
    }

    @Override
    public void punsubscribe(String... patterns) {
        pubSubCommands.punsubscribe(patterns);
    }

    @Override
    public void subscribe(String... channels) {
        pubSubCommands.subscribe(channels);
    }

    @Override
    public void unsubscribe(String... channels) {
        pubSubCommands.unsubscribe(channels);
    }

    public void addListener(RedisPubSubListener listener) {
        connection.addListener(new RedisClusterPubSubListener<>() {

            @Override
            public void message(RedisClusterNode node, String channel, byte[] message) {
                listener.message(channel, message);
            }

            @Override
            public void message(RedisClusterNode node, String pattern, String channel, byte[] message) {
                listener.message(pattern, channel, message);
            }

            @Override
            public void subscribed(RedisClusterNode node, String channel, long count) {
                listener.subscribed(channel, count);
            }

            @Override
            public void psubscribed(RedisClusterNode node, String pattern, long count) {
                listener.psubscribed(pattern, count);
            }

            @Override
            public void unsubscribed(RedisClusterNode node, String channel, long count) {
                listener.unsubscribed(channel, count);
            }

            @Override
            public void punsubscribed(RedisClusterNode node, String pattern, long count) {
                listener.punsubscribed(pattern, count);
            }
        });
    }

    @Override
    public Long publish(String channel, byte[] message) {
        return this.pubSubCommands.publish(channel, message);
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.connection);
    }

}