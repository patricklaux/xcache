package com.igeeksky.xcache.props;

/**
 * Redis 数据存储类型
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/11
 */
public enum RedisType {

    /**
     * 通过 Redis 的 StringCommands 来存值取值，即 set、mset、psetex、get、mget 等命令
     * <p>
     * 注意：如果采用 StringCommands，可以设置过期时间（expireAfterWrite）
     */
    STRING,

    /**
     * 通过 Redis 的 HashCommands 来存值取值，即 hset、hget、hmset、hmget、hdel 等命令
     * <p>
     * 注意：如果采用 HashCommands，无法设置过期时间
     */
    HASH

}
