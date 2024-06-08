package com.igeeksky.xcache.extension.serializer;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-07
 */
public abstract class AbstractSerializerProvider implements SerializerProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> Serializer<T> get(String name, Charset charset, Class<T> type, Class<?>[] valueParams) {
        if (Objects.equals(String.class, type)) {
            return (Serializer<T>) StringSerializer.getInstance(charset);
        }
        return doGet(charset, type, valueParams);
    }

    protected abstract <T> Serializer<T> doGet(Charset charset, Class<T> type, Class<?>[] valueParams);

}
