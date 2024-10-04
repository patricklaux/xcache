package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.RedisNode;
import com.igeeksky.redis.lettuce.config.LettuceClusterConfig;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.resource.ClientResources;

import java.util.ArrayList;
import java.util.List;

/**
 * Lettuce 集群客户端工厂
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public final class LettuceClusterFactory extends AbstractLettuceFactory {

    private final RedisClusterClient client;
    private final LettuceClusterConfig config;

    /**
     * 构造函数
     *
     * @param config  集群配置
     * @param options 集群客户端选项
     * @param res     客户端线程资源
     */
    public LettuceClusterFactory(LettuceClusterConfig config, ClusterClientOptions options, ClientResources res) {
        super();
        this.config = config;
        this.client = redisClient(options, res);
    }

    private RedisClusterClient redisClient(ClusterClientOptions options, ClientResources res) {
        List<RedisNode> nodes = config.getNodes();
        List<RedisURI> redisURIs = new ArrayList<>(nodes.size());
        for (RedisNode node : nodes) {
            redisURIs.add(LettuceHelper.redisURI(config, node));
        }

        RedisClusterClient redisClient = RedisClusterClient.create(res, redisURIs);
        redisClient.setOptions(options);
        return redisClient;
    }

    @Override
    protected LettuceClusterOperator createOperator() {
        StatefulRedisClusterConnection<byte[], byte[]> connection = connect(true);
        StatefulRedisClusterConnection<byte[], byte[]> batchConnection = connect(false);
        return new LettuceClusterOperator(config.getTimeout(), connection, batchConnection);
    }

    @Override
    protected LettuceStreamOperator createStreamOperator() {
        StatefulRedisClusterConnection<byte[], byte[]> connection = connect(true);
        return new LettuceStreamOperator(connection, connection.sync());
    }

    private StatefulRedisClusterConnection<byte[], byte[]> connect(boolean autoFlush) {
        var connection = client.connect(ByteArrayCodec.INSTANCE);
        connection.setReadFrom(config.getReadFrom());
        connection.setAutoFlushCommands(autoFlush);
        return connection;
    }

}