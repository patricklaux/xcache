package com.igeeksky.xcache.common;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2020-12-11
 */
public final class NullValue implements Serializable {

    public static final NullValue INSTANCE = new NullValue();
    public static final String INSTANCE_STR = "{\"@class\":\"com.igeeksky.xcache.common.NullValue\"}";
    public static final byte[] INSTANCE_BYTES = INSTANCE_STR.getBytes(StandardCharsets.UTF_8);

    private static final long serialVersionUID = 1L;

    private NullValue() {
    }

    private Object readResolve() {
        return INSTANCE;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || obj == null;
    }

    @Override
    public int hashCode() {
        return NullValue.class.hashCode();
    }

    @Override
    public String toString() {
        return "null";
    }
}
