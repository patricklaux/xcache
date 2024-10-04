package com.igeeksky.redis;

/**
 * Redis 配置异常
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-17
 */
public class RedisConfigException extends RuntimeException {

    /**
     * 无参构造
     */
    public RedisConfigException() {
        super();
    }

    /**
     * 带参构造
     *
     * @param message 异常信息
     */
    public RedisConfigException(String message) {
        super(message);
    }

    /**
     * 带参构造
     *
     * @param message 异常信息
     * @param cause   异常
     */
    public RedisConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
