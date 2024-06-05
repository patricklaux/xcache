package com.igeeksky.xcache.common;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-07
 */
public class CacheException extends RuntimeException {

    public CacheException() {
        super();
    }

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
