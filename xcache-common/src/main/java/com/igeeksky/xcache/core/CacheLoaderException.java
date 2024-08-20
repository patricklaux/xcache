package com.igeeksky.xcache.core;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-23
 */
public class CacheLoaderException extends RuntimeException {

    public CacheLoaderException(String message) {
        super(message);
    }

    public CacheLoaderException(Throwable cause) {
        super(cause);
    }

    public CacheLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

}
