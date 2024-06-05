package com.igeeksky.xcache.serialization.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igeeksky.xcache.extension.serializer.AbstractSerializerProvider;
import com.igeeksky.xcache.extension.serializer.Serializer;
import com.igeeksky.xtool.core.lang.ArrayUtils;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
public class JacksonSerializerProvider extends AbstractSerializerProvider {

    public static final JacksonSerializerProvider INSTANCE = new JacksonSerializerProvider();

    @Override
    public <T> Serializer<T> doGet(Charset charset, Class<T> type, Class<?>[] valueParams) {
        ObjectMapper mapper = new ObjectMapper();
        if (ArrayUtils.isEmpty(valueParams)) {
            return new JacksonSerializer<>(mapper, type, charset);
        }
        JavaType javaType = mapper.getTypeFactory().constructParametricType(type, valueParams);
        return new JacksonSerializer<>(mapper, javaType, charset);
    }

}
