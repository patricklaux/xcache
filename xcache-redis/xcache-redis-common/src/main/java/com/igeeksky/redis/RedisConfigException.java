package com.igeeksky.redis;

/**
 * Redis 命令执行异常
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-17
 */
public class RedisConfigException extends RuntimeException {

    public RedisConfigException() {
        super();
    }

    public RedisConfigException(String message) {
        super(message);
    }

    public RedisConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
