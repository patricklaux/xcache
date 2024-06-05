package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.core.config.HostAndPort;
import com.igeeksky.xcache.redis.RedisConnectionFactory;
import com.igeeksky.xcache.redis.lettuce.config.LettuceClusterConfig;
import com.igeeksky.xtool.core.io.IOUtils;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.resource.ClientResources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public class LettuceClusterConnectionFactory implements RedisConnectionFactory {

    private final Lock lock = new ReentrantLock();

    private final LettuceClusterConfig config;

    private final RedisClusterClient redisClient;

    private final LettuceClusterConnection lettuceConnection;

    private volatile LettuceClusterPubSubConnection pubSubConnection;

    public LettuceClusterConnectionFactory(LettuceClusterConfig config, ClusterClientOptions options, ClientResources res) {
        this.config = config;
        this.redisClient = redisClient(config, options, res);
        StatefulRedisClusterConnection<byte[], byte[]> connection = connection(redisClient, config, true);
        StatefulRedisClusterConnection<byte[], byte[]> bashConnection = connection(redisClient, config, false);
        RedisAdvancedClusterCommands<byte[], byte[]> commands = connection.sync();
        RedisAdvancedClusterReactiveCommands<byte[], byte[]> bashCommands = bashConnection.reactive();
        this.lettuceConnection = new LettuceClusterConnection(connection, commands, bashConnection, bashCommands);
    }

    private static StatefulRedisClusterConnection<byte[], byte[]> connection(
            RedisClusterClient redisClient, LettuceClusterConfig config, boolean autoFlush) {
        StatefulRedisClusterConnection<byte[], byte[]> connection = redisClient.connect(ByteArrayCodec.INSTANCE);
        connection.setReadFrom(config.getReadFrom());
        connection.setAutoFlushCommands(autoFlush);
        return connection;
    }

    private static RedisClusterClient redisClient(LettuceClusterConfig config, ClusterClientOptions options, ClientResources res) {
        List<RedisURI> redisURIS = new ArrayList<>();
        List<HostAndPort> nodes = config.getNodes();
        for (HostAndPort node : nodes) {
            redisURIS.add(LettuceHelper.redisURI(config, node));
        }
        RedisClusterClient redisClient = RedisClusterClient.create(res, redisURIS);
        redisClient.setOptions(options);
        return redisClient;
    }

    @Override
    public LettuceClusterConnection getConnection() {
        return this.lettuceConnection;
    }

    @Override
    public LettuceClusterPubSubConnection getPubSubConnection() {
        if (this.pubSubConnection == null) {
            ComposedRedisCodec codec = ComposedRedisCodec.getInstance(config.getCharset());
            lock.lock();
            try {
                if (this.pubSubConnection == null) {
                    this.pubSubConnection = new LettuceClusterPubSubConnection(redisClient.connectPubSub(codec));
                }
            } finally {
                lock.unlock();
            }
        }
        return this.pubSubConnection;
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.lettuceConnection, this.pubSubConnection);
    }

}