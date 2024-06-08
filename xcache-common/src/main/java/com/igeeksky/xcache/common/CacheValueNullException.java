package com.igeeksky.xcache.common;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-08
 */
public class CacheValueNullException extends CacheException {

    public CacheValueNullException() {
        super("Value must not be null");
    }

    public CacheValueNullException(String message) {
        super(message);
    }

    public CacheValueNullException(String message, Throwable cause) {
        super(message, cause);
    }

}
