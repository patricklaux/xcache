package com.igeeksky.redis;

/**
 * 脚本执行结果类别
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/13
 */
public enum ResultType {

    /**
     * Boolean output (expects a number {@code 0} or {@code 1} to be converted to a boolean value).
     */
    BOOLEAN,

    /**
     * {@link Long} output.
     */
    INTEGER,

    /**
     * List of flat arrays.
     */
    MULTI,

    /**
     * Simple status value such as {@code OK}. The Redis response is parsed as ASCII.
     */
    STATUS,

    /**
     * return byte[]
     */
    VALUE

}