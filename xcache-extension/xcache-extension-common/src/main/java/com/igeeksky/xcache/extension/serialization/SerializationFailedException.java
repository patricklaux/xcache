package com.igeeksky.xcache.extension.serialization;

/**
 * 序列化异常
 *
 * @author Patrick.Lau
 * @since 0.0.3 2020-12-11
 */
public class SerializationFailedException extends RuntimeException {

    public SerializationFailedException(String msg, Exception e) {
        super(msg, e);
    }

}
