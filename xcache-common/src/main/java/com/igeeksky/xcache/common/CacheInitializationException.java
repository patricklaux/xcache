package com.igeeksky.xcache.common;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-31
 */
public class CacheInitializationException extends RuntimeException {

    public CacheInitializationException() {
        super();
    }

    public CacheInitializationException(String message) {
        super(message);
    }

    public CacheInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheInitializationException(Throwable cause) {
        super(cause);
    }

}
