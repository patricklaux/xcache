package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.common.Provider;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisConnectionFactory extends Provider {

    RedisConnection getConnection();

    RedisPubSubConnection getPubSubConnection();

}
