package com.igeeksky.xcache;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * 空值对象
 * <p>
 * 用于序列化，反序列化，缓存中存储空值对象
 *
 * @author Patrick.Lau
 * @since 0.0.3 2020-12-11
 */
public final class NullValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final NullValue INSTANCE = new NullValue();

    public static final String INSTANCE_STR = "{\"@class\":\"com.igeeksky.xcache.NullValue\"}";

    public static final byte[] INSTANCE_BYTES = INSTANCE_STR.getBytes(StandardCharsets.UTF_8);

    private NullValue() {
    }

    @Serial
    private Object readResolve() {
        return INSTANCE;
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
