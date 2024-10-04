package com.igeeksky.redis;

import com.igeeksky.redis.stream.RedisStreamOperator;

/**
 * Redis 客户端工厂
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisOperatorFactory extends AutoCloseable {

    /**
     * 获取 Redis 客户端
     *
     * @return {@linkplain RedisOperator} – Redis 客户端
     */
    RedisOperator getRedisOperator();

    /**
     * 获取 Redis 流客户端
     *
     * @return {@linkplain RedisStreamOperator} – Redis 流客户端
     */
    RedisStreamOperator getRedisStreamOperator();

}