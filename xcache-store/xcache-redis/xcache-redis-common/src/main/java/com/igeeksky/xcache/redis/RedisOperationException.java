package com.igeeksky.xcache.redis;

/**
 * Redis 命令执行异常
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-17
 */
public class RedisOperationException extends RuntimeException {

    public RedisOperationException() {
        super();
    }

    public RedisOperationException(String message) {
        super(message);
    }

    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
