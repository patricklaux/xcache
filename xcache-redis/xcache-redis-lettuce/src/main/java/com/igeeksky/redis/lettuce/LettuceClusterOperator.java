package com.igeeksky.redis.lettuce;

import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;

/**
 * Lettuce 集群客户端
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public final class LettuceClusterOperator extends AbstractLettuceOperator {

    /**
     * 构造函数
     *
     * @param timeout         超时时间（毫秒）
     * @param connection      连接对象（执行单个命令，自动提交）
     * @param batchConnection 连接对象（批量执行命令，不自动提交）
     */
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