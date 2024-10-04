package com.igeeksky.redis;

/**
 * Redis 命令执行异常
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-17
 */
public class RedisOperationException extends RuntimeException {

    /**
     * 无参构造器
     */
    public RedisOperationException() {
        super();
    }

    /**
     * 带异常信息的构造器
     *
     * @param message 异常信息
     */
    public RedisOperationException(String message) {
        super(message);
    }

    /**
     * 带异常信息和异常的构造器
     *
     * @param message 错误消息
     * @param cause   异常
     */
    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
