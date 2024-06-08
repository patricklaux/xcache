package com.igeeksky.xcache.extension.compress;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-07
 */
public class CacheValueCompressException extends RuntimeException {

    public CacheValueCompressException() {
        super();
    }

    public CacheValueCompressException(String message) {
        super(message);
    }

    public CacheValueCompressException(String message, Throwable cause) {
        super(message, cause);
    }
}
