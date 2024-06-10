package com.igeeksky.xcache.redis.lettuce;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * 单机 Redis 或 主从/哨兵 Redis 连接
 * <p>
 * 特别说明：因为不执行事务和阻塞命令，无需连接池
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public class LettuceConnection extends AbstractLettuceConnection {

    public LettuceConnection(StatefulRedisConnection<byte[], byte[]> connection,
                             RedisCommands<byte[], byte[]> commands,
                             StatefulRedisConnection<byte[], byte[]> bashConnection,
                             RedisAsyncCommands<byte[], byte[]> bashCommands) {
        super(connection, commands, commands, commands, bashConnection, bashCommands);
    }

    @Override
    public boolean isCluster() {
        return false;
    }
}
