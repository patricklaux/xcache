package com.igeeksky.xcache.common;

/**
 * 缓存配置异常
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-19
 */
public class CacheConfigException extends RuntimeException {

    public CacheConfigException() {
        super();
    }

    public CacheConfigException(String message) {
        super(message);
    }

    public CacheConfigException(Throwable cause) {
        super(cause);
    }

}