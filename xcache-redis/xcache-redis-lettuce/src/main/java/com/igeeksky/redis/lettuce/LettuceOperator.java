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

    /**
     * 构造函数
     *
     * @param timeout         超时时间（毫秒）
     * @param connection      连接对象（执行单个命令，自动提交）
     * @param batchConnection 连接对象（批量执行命令，不自动提交）
     */
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
