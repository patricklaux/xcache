package com.igeeksky.xcache.redis.lettuce;

import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public class LettuceClusterConnection extends AbstractLettuceConnection {

    public LettuceClusterConnection(StatefulRedisClusterConnection<byte[], byte[]> connection,
                                    RedisAdvancedClusterCommands<byte[], byte[]> commands,
                                    StatefulRedisClusterConnection<byte[], byte[]> bashConnection,
                                    RedisAdvancedClusterReactiveCommands<byte[], byte[]> bashCommands) {
        super(connection, commands, commands, commands, bashConnection, bashCommands);
    }

    @Override
    public boolean isCluster() {
        return true;
    }
}