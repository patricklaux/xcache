package com.igeeksky.xcache.common;

/**
 * 数据源读取数据异常
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-23
 */
public class CacheLoadingException extends RuntimeException {

    public CacheLoadingException(String message) {
        super(message);
    }

    public CacheLoadingException(Throwable cause) {
        super(cause);
    }

    public CacheLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

}
