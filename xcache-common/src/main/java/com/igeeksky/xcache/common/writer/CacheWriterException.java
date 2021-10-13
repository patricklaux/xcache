package com.igeeksky.xcache.common.writer;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-23
 */
public class CacheWriterException extends RuntimeException {

    public CacheWriterException(String message) {
        super(message);
    }

    public CacheWriterException(Throwable cause) {
        super(cause);
    }

    public CacheWriterException(String message, Throwable cause) {
        super(message, cause);
    }

}
