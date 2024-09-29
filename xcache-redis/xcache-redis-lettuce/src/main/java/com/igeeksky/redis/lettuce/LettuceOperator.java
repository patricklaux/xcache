package com.igeeksky.redis.lettuce;

import io.lettuce.core.api.StatefulRedisConnection;

/**
 * lettuce 客户端实现类
 * <p>
 * 支持（非集群）：standalone 及 sentinel 模式
 * <p>
 * 特别说明：无事务和阻塞命令，因此无需连接池
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public final class LettuceOperator extends AbstractLettuceOperator {

    public LettuceOperator(long timeout,
                           StatefulRedisConnection<byte[], byte[]> connection,
                           StatefulRedisConnection<byte[], byte[]> batchConnection) {
        super(timeout, connection, batchConnection);
    }

    @Override
    public boolean isCluster() {
        return false;
    }
}
