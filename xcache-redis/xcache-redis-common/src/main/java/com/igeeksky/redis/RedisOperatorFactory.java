package com.igeeksky.redis;

import com.igeeksky.redis.stream.RedisStreamOperator;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisOperatorFactory extends AutoCloseable {

    RedisOperator getRedisOperator();

    RedisStreamOperator getRedisStreamOperator();

}