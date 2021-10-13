package com.igeeksky.xcache.serialization.jackson;

import com.igeeksky.xcache.common.Singleton;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xcache.extension.serialization.SerializerProvider;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @date 2021-06-22
 */
@Singleton
public class Jackson2JsonSerializerProvider implements SerializerProvider {

    @Override
    public <T> Serializer<T> get(Class<T> clazz, Charset charset) {
        return new Jackson2JsonSerializer<>(clazz);
    }

}
