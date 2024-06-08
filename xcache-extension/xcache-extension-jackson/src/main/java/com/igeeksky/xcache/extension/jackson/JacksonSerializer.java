package com.igeeksky.xcache.extension.jackson;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.igeeksky.xcache.extension.serializer.SerializationFailedException;
import com.igeeksky.xcache.extension.serializer.Serializer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;


/**
 * Jackson序列化器
 *
 * @author Patrick.Lau
 * @since 0.0.1 2017-03-06
 */
public class JacksonSerializer<T> implements Serializer<T> {

    private final Charset charset;
    private final JavaType javaType;
    private final ObjectMapper mapper;

    public JacksonSerializer(Class<T> type, Charset charset) {
        this(new ObjectMapper(), type, charset);
    }

    public JacksonSerializer(ObjectMapper mapper, Class<T> type, Charset charset) {
        this(mapper, TypeFactory.defaultInstance().constructType(type), charset);
    }

    public JacksonSerializer(ObjectMapper mapper, JavaType javaType, Charset charset) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        Objects.requireNonNull(javaType, "javaType must not be null");
        Objects.requireNonNull(charset, "charset must not be null");
        this.mapper = mapper;
        this.charset = charset;
        this.javaType = javaType;
    }

    @Override
    public byte[] serialize(T obj) {
        if (null == obj) {
            throw new SerializationFailedException("obj must not be null");
        }
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            String msg = String.format("Could not write to JSON: [%s]. %s", obj, e.getMessage());
            throw new SerializationFailedException(msg, e);
        }
    }

    @Override
    public T deserialize(byte[] source) {
        if (null == source) {
            throw new SerializationFailedException("byte[] source must not be null");
        }
        try {
            return mapper.readValue(source, javaType);
        } catch (IOException e) {
            String msg = String.format("Could not read from JSON: [%s]. %s", new String(source, charset), e.getMessage());
            throw new SerializationFailedException(msg, e);
        }
    }
}
