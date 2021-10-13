package com.igeeksky.xcache.serialization.jackson;

import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xcache.extension.serialization.SerializerProvider;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-26
 */
public class GenericJackson2JsonSerializerProvider implements SerializerProvider {

    private static final GenericJackson2JsonSerializer SERIALIZER = new GenericJackson2JsonSerializer();

    @Override
    public <T> Serializer<T> get(Class<T> clazz, Charset charset) {
        return (Serializer<T>) SERIALIZER;
    }
}
