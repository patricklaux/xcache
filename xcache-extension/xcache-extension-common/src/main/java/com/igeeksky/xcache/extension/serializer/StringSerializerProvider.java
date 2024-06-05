package com.igeeksky.xcache.extension.serializer;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-20
 */
public class StringSerializerProvider implements SerializerProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> Serializer<T> get(String name, Charset charset, Class<T> type, Class<?>[] valueParams) {
        if (Objects.equals(String.class, type)) {
            return (Serializer<T>) StringSerializer.getInstance(charset);
        }
        throw new IllegalArgumentException("type must be String.class. " + type);
    }

}