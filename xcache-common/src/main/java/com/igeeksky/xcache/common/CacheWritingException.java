package com.igeeksky.xcache.common;

/**
 * 数据源写数据异常
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-23
 */
public class CacheWritingException extends RuntimeException {

    public CacheWritingException(String message) {
        super(message);
    }

    public CacheWritingException(Throwable cause) {
        super(cause);
    }

    public CacheWritingException(String message, Throwable cause) {
        super(message, cause);
    }

}
