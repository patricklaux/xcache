package com.igeeksky.xcache.extension;

/**
 * @author Patrick.Lau
 * @date 2021-07-26
 */
public class BeanInstantiationException extends RuntimeException {

    public BeanInstantiationException(String message) {
        super(message);
    }

    public BeanInstantiationException(ReflectiveOperationException e) {
        super(e);
    }

    public BeanInstantiationException(String message, ReflectiveOperationException e) {
        super(message, e);
    }
}
