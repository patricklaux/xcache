package com.igeeksky.xcache.extension.jackson;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.CodecException;

import java.io.IOException;
import java.util.Objects;

/**
 * Jackson 序列化
 *
 * @author Patrick.Lau
 * @since 0.0.1 2017-03-06
 */
public class JacksonCodec<V> implements Codec<V> {

    private final JavaType javaType;
    private final ObjectMapper mapper;

    public JacksonCodec(Class<V> type) {
        this(new ObjectMapper(), type);
    }

    public JacksonCodec(ObjectMapper mapper, Class<V> type) {
        this(mapper, TypeFactory.defaultInstance().constructType(type));
    }

    public JacksonCodec(ObjectMapper mapper, JavaType javaType) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        Objects.requireNonNull(javaType, "javaType must not be null");
        this.mapper = mapper;
        this.javaType = javaType;
    }

    @Override
    public byte[] encode(V obj) {
        if (null == obj) {
            throw new CodecException("obj must not be null");
        }
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            String msg = String.format("Unable to write to JSON: [%s]. %s", obj, e.getMessage());
            throw new CodecException(msg, e);
        }
    }

    @Override
    public V decode(byte[] source) {
        if (null == source) {
            throw new CodecException("byte[] source must not be null");
        }
        try {
            return mapper.readValue(source, javaType);
        } catch (IOException e) {
            String msg = String.format("Unable to read from JSON. %s", e.getMessage());
            throw new CodecException(msg, e);
        }
    }

}