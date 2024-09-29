package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.lettuce.config.LettuceSentinelConfig;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.resource.ClientResources;

/**
 * Lettuce Sentinel 客户端工厂
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public final class LettuceSentinelFactory extends AbstractLettuceFactory {

    private final RedisURI uri;
    private final RedisClient client;
    private final LettuceSentinelConfig config;

    public LettuceSentinelFactory(LettuceSentinelConfig config, ClientOptions options, ClientResources res) {
        super();
        this.config = config;
        this.uri = redisUri(config);
        this.client = redisClient(options, res);
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

    private static RedisURI redisUri(LettuceSentinelConfig config) {
        return LettuceHelper.sentinelURIBuilder(config);
    }

    private static RedisClient redisClient(ClientOptions options, ClientResources res) {
        RedisClient redisClient = RedisClient.create(res);
        redisClient.setOptions(options);
        return redisClient;
    }

    private StatefulRedisMasterReplicaConnection<byte[], byte[]> connect(boolean autoFlush) {
        ByteArrayCodec codec = ByteArrayCodec.INSTANCE;
        StatefulRedisMasterReplicaConnection<byte[], byte[]> connection = MasterReplica.connect(client, codec, uri);
        connection.setReadFrom(config.getReadFrom());
        connection.setAutoFlushCommands(autoFlush);
        return connection;
    }

}