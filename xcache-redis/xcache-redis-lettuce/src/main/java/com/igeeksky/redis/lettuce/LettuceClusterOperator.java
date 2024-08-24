package com.igeeksky.redis.lettuce;

import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public class LettuceClusterOperator extends AbstractLettuceOperator {

    public LettuceClusterOperator(long timeout,
                                  StatefulRedisClusterConnection<byte[], byte[]> connection,
                                  StatefulRedisClusterConnection<byte[], byte[]> batchConnection) {
        super(timeout, connection, batchConnection);
    }

    @Override
    public boolean isCluster() {
        return true;
    }

}