package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.RedisConfigException;
import com.igeeksky.redis.RedisNode;
import com.igeeksky.redis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.resource.ClientResources;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public class LettuceFactory extends AbstractLettuceFactory {

    private final RedisClient client;
    private final LettuceStandaloneConfig config;

    public LettuceFactory(LettuceStandaloneConfig config, ClientOptions options, ClientResources resources) {
        this.config = config;
        this.client = redisClient(resources, options);
    }

    private static RedisClient redisClient(ClientResources resources, ClientOptions options) {
        RedisClient redisClient = RedisClient.create(resources);
        redisClient.setOptions(options);
        return redisClient;
    }

    @Override
    protected LettuceOperator createOperator() {
        StatefulRedisMasterReplicaConnection<byte[], byte[]> baseConnection = connect(true);
        StatefulRedisMasterReplicaConnection<byte[], byte[]> batchConnection = connect(false);
        return new LettuceOperator(config.getTimeout(), baseConnection, batchConnection);
    }

    @Override
    protected LettuceStreamOperator createStreamOperator() {
        StatefulRedisMasterReplicaConnection<byte[], byte[]> connection = connect(true);
        return new LettuceStreamOperator(connection, connection.sync());
    }

    private StatefulRedisMasterReplicaConnection<byte[], byte[]> connect(boolean autoFlush) {
        RedisURI redisURI = null;
        if (config.getNode() != null) {
            redisURI = LettuceHelper.redisURI(config, config.getNode());
        }

        ByteArrayCodec codec = ByteArrayCodec.INSTANCE;

        // 创建 Standalone[主从连接]，未配置副本节点，动态拓扑结构，主动发现副本集
        List<RedisNode> nodes = config.getNodes();
        if (CollectionUtils.isEmpty(nodes)) {
            if (redisURI != null) {
                StatefulRedisMasterReplicaConnection<byte[], byte[]> conn = MasterReplica.connect(client, codec, redisURI);
                conn.setReadFrom(config.getReadFrom());
                conn.setAutoFlushCommands(autoFlush);
                return conn;
            }
            throw new RedisConfigException("Redis standalone: id:[" + config.getId() + "] No nodes configured");
        }

        // 创建 Standalone[主从连接]，已配置副本节点，静态拓扑结构
        List<RedisURI> redisURIS = new ArrayList<>();
        if (redisURI != null) {
            redisURIS.add(redisURI);
        }

        for (RedisNode node : nodes) {
            redisURIS.add(LettuceHelper.redisURI(config, node));
        }

        StatefulRedisMasterReplicaConnection<byte[], byte[]> conn = MasterReplica.connect(client, codec, redisURIS);
        conn.setReadFrom(config.getReadFrom());
        conn.setAutoFlushCommands(autoFlush);
        return conn;
    }

}