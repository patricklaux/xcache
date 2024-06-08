package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.core.config.HostAndPort;
import com.igeeksky.xcache.redis.RedisConnectionFactory;
import com.igeeksky.xcache.redis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.io.IOUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.resource.ClientResources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public class LettuceConnectionFactory implements RedisConnectionFactory {

    private final Lock lock = new ReentrantLock();

    private final RedisClient client;

    private final LettuceStandaloneConfig config;

    private final LettuceConnection lettuceConnection;

    private volatile LettucePubSubConnection pubSubConnection;

    public LettuceConnectionFactory(LettuceStandaloneConfig config, ClientOptions options, ClientResources resources) {
        this.config = config;
        this.client = redisClient(resources, options);
        StatefulRedisMasterReplicaConnection<byte[], byte[]> connection = connection(true);
        StatefulRedisMasterReplicaConnection<byte[], byte[]> bashConnection = connection(false);
        RedisCommands<byte[], byte[]> commands = connection.sync();
        RedisReactiveCommands<byte[], byte[]> bashCommands = bashConnection.reactive();
        this.lettuceConnection = new LettuceConnection(connection, commands, bashConnection, bashCommands);
    }

    private StatefulRedisMasterReplicaConnection<byte[], byte[]> connection(boolean autoFlush) {
        RedisURI redisURI = LettuceHelper.redisURI(config, config.getMaster());

        ByteArrayCodec codec = ByteArrayCodec.INSTANCE;

        // 创建 Standalone[主从连接]，未配置副本节点，动态拓扑结构，主动发现副本集
        List<HostAndPort> replicas = config.getReplicas();
        if (CollectionUtils.isEmpty(replicas)) {
            StatefulRedisMasterReplicaConnection<byte[], byte[]> conn = MasterReplica.connect(client, codec, redisURI);
            conn.setReadFrom(config.getReadFrom());
            conn.setAutoFlushCommands(autoFlush);
            return conn;
        }

        // 创建 Standalone[主从连接]，已配置副本节点，静态拓扑结构
        List<RedisURI> redisURIS = new ArrayList<>();
        redisURIS.add(redisURI);
        for (HostAndPort replica : replicas) {
            redisURIS.add(LettuceHelper.redisURI(config, replica));
        }

        StatefulRedisMasterReplicaConnection<byte[], byte[]> conn = MasterReplica.connect(client, codec, redisURIS);
        conn.setReadFrom(config.getReadFrom());
        conn.setAutoFlushCommands(autoFlush);
        return conn;
    }

    private static RedisClient redisClient(ClientResources resources, ClientOptions options) {
        RedisClient redisClient = RedisClient.create(resources);
        redisClient.setOptions(options);
        return redisClient;
    }

    @Override
    public LettuceConnection getConnection() {
        return this.lettuceConnection;
    }

    @Override
    public LettucePubSubConnection getPubSubConnection() {
        if (this.pubSubConnection == null) {
            lock.lock();
            try {
                if (this.pubSubConnection == null) {
                    this.pubSubConnection = new LettucePubSubConnection(pubSubConnection());
                }
            } finally {
                lock.unlock();
            }
        }
        return this.pubSubConnection;
    }

    private StatefulRedisPubSubConnection<String, byte[]> pubSubConnection() {
        RedisURI redisURI = LettuceHelper.redisURI(config, config.getMaster());
        return client.connectPubSub(ComposedRedisCodec.getInstance(config.getCharset()), redisURI);
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.lettuceConnection, this.pubSubConnection);
    }

}