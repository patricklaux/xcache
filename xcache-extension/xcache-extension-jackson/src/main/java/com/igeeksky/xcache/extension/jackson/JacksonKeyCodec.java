package com.igeeksky.xcache.extension.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.igeeksky.xtool.core.lang.codec.CodecException;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-15
 */
public class JacksonKeyCodec<K> implements KeyCodec<K> {

    private final JavaType javaType;
    private final ObjectMapper mapper;

    public JacksonKeyCodec(Class<K> type) {
        this.mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.javaType = TypeFactory.defaultInstance().constructType(type);
    }

    public JacksonKeyCodec(ObjectMapper mapper, Class<K> type) {
        this(mapper, TypeFactory.defaultInstance().constructType(type));
    }

    public JacksonKeyCodec(ObjectMapper mapper, JavaType javaType) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        Objects.requireNonNull(javaType, "javaType must not be null");
        this.mapper = mapper;
        this.javaType = javaType;
    }

    @Override
    public String encode(K key) {
        if (null == key) {
            throw new CodecException("obj must not be null");
        }
        try {
            return mapper.writeValueAsString(key);
        } catch (JsonProcessingException e) {
            String msg = String.format("Unable to write to JSON: [%s]. %s", key, e.getMessage());
            throw new CodecException(msg, e);
        }
    }

    @Override
    public K decode(String source) {
        if (null == source) {
            throw new CodecException("source must not be null");
        }
        try {
            return mapper.readValue(source, javaType);
        } catch (IOException e) {
            String msg = String.format("Unable to read from JSON: [%s]. %s", source, e.getMessage());
            throw new CodecException(msg, e);
        }
    }

}