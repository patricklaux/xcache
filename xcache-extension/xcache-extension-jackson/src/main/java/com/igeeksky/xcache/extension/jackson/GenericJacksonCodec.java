package com.igeeksky.xcache.extension.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.igeeksky.xtool.core.lang.StringUtils;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.CodecException;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用的序列化器
 * <p>
 * 生成的 Json 对象包含 Java 类型信息，适用于 Spring Cache
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/10
 */
public class GenericJacksonCodec implements Codec<Object> {

    private static final GenericJacksonCodec INSTANCE = new GenericJacksonCodec(new ObjectMapper(), null);

    private final ObjectMapper mapper;
    private final Map<JavaTypeInfo, JavaType> typeCache = new ConcurrentHashMap<>();

    public GenericJacksonCodec(ObjectMapper mapper, String propertyName) {
        Objects.requireNonNull(mapper, "ObjectMapper must not be null!");
        this.mapper = mapper;
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        PolymorphicTypeValidator ptv = this.mapper.getPolymorphicTypeValidator();
        if (StringUtils.hasText(propertyName)) {
            mapper.activateDefaultTypingAsProperty(ptv, DefaultTyping.NON_FINAL, propertyName);
        } else {
            mapper.activateDefaultTyping(ptv, DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        }
    }

    public static GenericJacksonCodec getInstance() {
        return INSTANCE;
    }

    @Override
    public byte[] encode(Object source) {
        if (source == null) {
            throw new CodecException("Value must not be null!");
        }
        try {
            return mapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            String msg = String.format("Unable to write to JSON: [%s]. %s", source, e.getMessage());
            throw new CodecException(msg, e);
        }
    }

    @Override
    public Object decode(byte[] source) {
        return decode(source, Object.class, null);
    }

    public Object decode(byte[] source, Class<?> type, Class<?>[] params) {
        if (null == source) {
            throw new CodecException("byte[] source must not be null");
        }
        if (type == null) {
            throw new CodecException("Class<?> type must not be null");
        }
        try {
            return mapper.readValue(source, getJavaType(type, params));
        } catch (IOException e) {
            String msg = String.format("Unable to read from JSON. %s", e.getMessage());
            throw new CodecException(msg, e);
        }
    }

    private JavaType getJavaType(Class<?> type, Class<?>[] params) {
        return typeCache.computeIfAbsent(new JavaTypeInfo(type, params), key -> {
            if (params == null || params.length == 0) {
                return mapper.getTypeFactory().constructType(type);
            }
            return mapper.getTypeFactory().constructParametricType(type, params);
        });
    }

}