package com.igeeksky.xcache.extension.serialization;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-23
 */
public class JdkSerializerProvider implements SerializerProvider {

    @Override
    public <T> Serializer<T> get(Class<T> clazz, Charset charset) {
        return new JdkSerializer<>();
    }

}
